package com.example.velomap

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapView
import com.mapbox.maps.RenderedQueryGeometry
import com.mapbox.maps.RenderedQueryOptions
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.interpolate
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var layerIds:List<String>
    private lateinit var polygonsList: List<PolygonData>

    private val googleSheetsService = GoogleSheetsService("AIzaSyBW5UaZZJgkHLS5WGvr3R6kUsy4vea3xcE", this)
    private val LOCATION_PERMISSION_REQUEST_CODE = 100

    lateinit var viewModel: MainViewModel
    private val polygonInfoMap = mutableMapOf<String, PolygonInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        mapView = findViewById(R.id.mapView)
        findViewById<Button>(R.id.search_button).setOnClickListener {
            setupSearchButton()
//            val polygonId = findViewById<EditText>(R.id.polygon_id_input).text.toString()
//            searchPolygon(polygonId)
        }

        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) { style ->

            viewModel.polygonInfo.observe(this) { polygonsInfo ->
                val geoJsonString = assets.open("polygons.geojson")
                    .bufferedReader()
                    .use { it.readText() }
                lifecycleScope.launch {
                    polygonsList = parseGeoJson(geoJsonString)
                }


                layerIds = PolygonColorUtils.applyPolygonColors(geoJsonString, polygonsInfo, style)

                polygonsInfo.forEach { info ->
                    polygonInfoMap[info.id] = info
                }

            }
            lifecycleScope.launch {
                viewModel.fetchPolygonInfo(googleSheetsService)
            }
        }

        mapView.getMapboxMap().addOnMapClickListener { point ->
            val screenPoint = mapView.getMapboxMap().pixelForCoordinate(point)
            val queryGeometry = RenderedQueryGeometry(screenPoint)

            // Используем список названий слоев для запроса
            val queryOptions = RenderedQueryOptions(layerIds, null)
            mapView.getMapboxMap().queryRenderedFeatures(
                queryGeometry, queryOptions
            ) { features ->
                if (features.value?.isNotEmpty() == true) {
                    val queriedFeature = features.value?.firstOrNull()
                    queriedFeature?.let {
                        val properties = it.queriedFeature.feature.properties()
                        val iid = properties?.get("iid")
                            ?.asString
                            ?.replace("\"", "")
                            ?.trim() ?: "Неизвестный"
                        val polygonInfo = polygonInfoMap[iid]
                        Log.d("GeoJSON", polygonInfoMap.toString())
                        Log.d("GeoJSON", polygonInfo.toString())
                        if (polygonInfo != null) {
                            // Выводим полную информацию о полигоне
                            val dialog = PolygonInfoDialogFragment.newInstance(polygonInfo)
                            dialog.show(supportFragmentManager, "PolygonInfoDialog")
                        } else {
                            Toast.makeText(this, "Информация о полигоне не найдена", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Полигон не найден", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }

        enableLocationComponent()

        findViewById<Button>(R.id.location_button).setOnClickListener {

            val locationComponentPlugin = mapView.location

            locationComponentPlugin.addOnIndicatorPositionChangedListener(object :
                OnIndicatorPositionChangedListener {
                override fun onIndicatorPositionChanged(point: Point) {
                    mapView.getMapboxMap().setCamera(
                        CameraOptions.Builder()
                            .center(point)
                            .zoom(14.0)
                            .build()
                    )

                    locationComponentPlugin.removeOnIndicatorPositionChangedListener(this)
                }
            })
        }
    }


    private fun enableLocationComponent() {
        // Check for location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED) {
            initLocationComponent()
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }


    private fun initLocationComponent() {
        val locationComponentPlugin = mapView.location
        locationComponentPlugin.updateSettings {
            puckBearing = PuckBearing.COURSE
            puckBearingEnabled = true
            enabled = true
            locationPuck = LocationPuck2D(
                bearingImage = ImageHolder.from(R.drawable.ic_launcher_foreground),
                shadowImage = ImageHolder.from(R.drawable.ic_launcher_foreground),
                scaleExpression = interpolate {
                    linear()
                    zoom()
                    stop(0.0, 0.6)
                    stop(20.0, 1.0)
                }.toJson()
            )
        }
    }
    private fun setupSearchButton() {
        val searchButton = findViewById<Button>(R.id.search_button) // ID вашей кнопки поиска
        val searchInput = findViewById<EditText>(R.id.polygon_id_input) // ID поля ввода

        searchButton.setOnClickListener {
            val polygonId = searchInput.text.toString().trim() // Получаем введенный ID
            Log.d("Search", polygonId)
            Log.d("Search", polygonsList.toString())

            // Ищем полигон по ID
            val polygonData = polygonsList.find { it.id == polygonId } // Здесь polygonsList - ваш список из parseGeoJson
            if (polygonData != null) {
                val centroid = polygonData.centroid // Получаем центроид

                // Зуммируем к центроиду
                mapView.getMapboxMap().setCamera(
                    CameraOptions.Builder()
                        .center(centroid)
                        .zoom(16.0) // Уровень зума, можете изменить по необходимости
                        .build()
                )
            } else {
                Toast.makeText(this, "Полигон с ID $polygonId не найден", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initLocationComponent()
        } else {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }
}