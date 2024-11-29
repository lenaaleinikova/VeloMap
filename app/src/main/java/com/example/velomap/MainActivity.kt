package com.example.velomap


import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.velomap.data.PolygonData
import com.example.velomap.data.PolygonInfo
import com.example.velomap.network.GoogleSheetsService
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions

import com.mapbox.maps.MapView
import com.mapbox.maps.RenderedQueryGeometry
import com.mapbox.maps.RenderedQueryOptions

import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var layerIds: List<String>
    private lateinit var polygonsList: List<PolygonData>

    private lateinit var viewModel: MainViewModel
    private lateinit var mapManager: MapManager

    private val googleSheetsService =
        GoogleSheetsService("AIzaSyBW5UaZZJgkHLS5WGvr3R6kUsy4vea3xcE", this)
    private val LOCATION_PERMISSION_REQUEST_CODE = 100


    private val polygonInfoMap = mutableMapOf<String, PolygonInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        mapView = findViewById(R.id.mapView)

        val repository =
            PolygonRepository(GoogleSheetsService("AIzaSyBW5UaZZJgkHLS5WGvr3R6kUsy4vea3xcE", this))
        viewModel = ViewModelProvider(this, ViewModelFactory(repository))[MainViewModel::class.java]
        mapManager = MapManager(mapView, this)

        Log.d("Mainload", "load")

        observeViewModel()
        viewModel.fetchPolygonInfo()


        findViewById<ImageButton>(R.id.search_button).setOnClickListener {
            setupSearchButton()
        }

        MapUtils.enableLocationComponent(this, mapView)

        findViewById<ImageButton>(R.id.location_button).setOnClickListener {

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


    private fun observeViewModel() {
        Log.d("Mainload", "observeViewModel")
        viewModel.polygonInfo.observe(this) { result ->
            Log.d("Mainload", "result ${result.toString()}")
            result.onSuccess { polygons ->
                val geoJsonString =
                    assets.open("polygons.geojson").bufferedReader().use { it.readText() }
//                Log.d("Mainload", "2 $geoJsonString")
                Log.d("Mainload", "polygons $polygons")
                lifecycleScope.launch {
                    polygonsList = parseGeoJson(geoJsonString)
                }
                val layerIds = mapManager.loadStyle(polygons, geoJsonString)
                Log.d("Mainload", "layerIds $layerIds")
                setupMapInteractions(layerIds, polygons)
            }
            result.onFailure {
                Toast.makeText(this, "Failed to load polygon info", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupMapInteractions(layerIds: List<String>, polygons: List<PolygonInfo>) {

        mapView.getMapboxMap().addOnMapClickListener { point ->

            val screenPoint = mapView.getMapboxMap().pixelForCoordinate(point)
            val queryGeometry = RenderedQueryGeometry(screenPoint)


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

                        val polygonInfo = polygons.find { polygon -> polygon.id == iid }
                        if (polygonInfo != null) {
                            val dialog = PolygonInfoDialogFragment.newInstance(polygonInfo)
                            dialog.show(supportFragmentManager, "PolygonInfoDialog")
                        } else {
                            Toast.makeText(
                                this,
                                "Информация о полигоне не найдена",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Полигон не найден", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }
    }


    private fun setupSearchButton() {
        val searchButton = findViewById<ImageButton>(R.id.search_button)
        val searchInput = findViewById<EditText>(R.id.polygon_id_input)

        searchButton.setOnClickListener {
            val polygonId = searchInput.text.toString().trim()
            Log.d("Search", polygonId)
            Log.d("Search", polygonsList.toString())


            val polygonData = polygonsList.find { it.id == polygonId }
            if (polygonData != null) {
                val centroid = polygonData.centroid


                mapView.getMapboxMap().setCamera(
                    CameraOptions.Builder()
                        .center(centroid)
                        .zoom(16.0)
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
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            MapUtils.initLocationComponent(mapView)
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