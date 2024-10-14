package com.example.velomap

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.eq
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.get
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.literal
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.fillLayer
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor
import com.mapbox.maps.extension.style.layers.properties.generated.TextJustify
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import kotlinx.coroutines.launch
import android.Manifest
import android.widget.Button
import com.google.android.gms.maps.model.LatLng
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.locationcomponent.*
import com.mapbox.geojson.Point



class MainActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var style: Style

    private val googleSheetsService = GoogleSheetsService("AIzaSyBW5UaZZJgkHLS5WGvr3R6kUsy4vea3xcE")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.mapView)


        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) { style ->
            lifecycleScope.launch {
                val statuses = googleSheetsService.fetchStatuses() // Получаем данные из Google Sheets
                val geoJsonString = assets.open("polygons.geojson")
                    .bufferedReader()
                    .use { it.readText() }

                applyPolygonColors(geoJsonString, statuses, style) // Раскрашиваем полигоны
            }
        }
        findViewById<Button>(R.id.location_button).setOnClickListener {
            val locationComponent = mapView.location
            locationComponent.addOnIndicatorPositionChangedListener(object : OnIndicatorPositionChangedListener {
                override fun onIndicatorPositionChanged(point: Point) {
                    // Как только местоположение получено, центрируем карту
                    mapView.getMapboxMap().setCamera(CameraOptions.Builder()
                        .center(point) // Устанавливаем центр карты на текущее местоположение
                        .zoom(14.0)    // Устанавливаем зум
                        .build())

                    // Убираем слушатель, чтобы отслеживание не продолжалось после первого получения позиции
                    locationComponent.removeOnIndicatorPositionChangedListener(this)
                }
            })
        }

    }

    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            enableLocationComponent()
        }
    }

    private fun enableLocationComponent() {
        val locationComponentPlugin = mapView.location // Получаем доступ к компоненту локации
        locationComponentPlugin.updateSettings {
            enabled = true
            pulsingEnabled = true // Включаем пульсирующий индикатор локации
        }
    }




    private fun applyPolygonColors(
        geoJson: String,
        statuses: List<Pair<String, String>>,
        style: Style
    ) {
        val geoJsonSource = geoJsonSource("polygon-source") {
            data(geoJson)
        }

        style.addSource(geoJsonSource)
        Log.d("GeoJSON", "status "+statuses.toString())

        statuses.forEach { (iid, status) ->
            val color = when (status) {
                "1" -> "#00FF00" // Зеленый
                "Принято" -> "#0000FF" // Голубой
                "не снято" -> "#FF0000" // Красный
                else -> "#800080" // Фиолетовый
            }

            style.addLayer(
                fillLayer("polygon-layer-$iid", "polygon-source") {
                    filter(eq(get("iid"), literal(iid))) // Фильтруем по 'iid'
                    fillColor(color) // Устанавливаем цвет
                    fillOpacity(0.5)
                }
            )

            style.addLayer(
                lineLayer("polygon-outline-layer-$iid", "polygon-source") {
                    filter(eq(get("iid"), literal(iid))) // Фильтруем по 'iid'
                    lineColor("#000000") // Черный цвет обводки
                    lineWidth(1.0) // Ширина линии обводки
                    minZoom(10.0)
                }
            )

        }
        style.addLayer(
            symbolLayer("polygon-label-layer", "polygon-source") {
                textField("{iid}") // Используем значение из поля "iid"
                textSize(14.0)
                textColor("#000000") // Черный цвет текста
                textJustify(TextJustify.CENTER)
                textAnchor(TextAnchor.CENTER)
                textIgnorePlacement(true) // Игнорируем перекрытие
                textAllowOverlap(true)

                minZoom(13.0) // Минимальный зум, при котором будут видны подписи
                //maxZoom(16.0) // Максимальный зум, при котором подписи будут видны
            }
        )
    }
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableLocationComponent()
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