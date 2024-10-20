package com.example.velomap

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
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
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.interpolate
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var mapView: MapView

    private val googleSheetsService = GoogleSheetsService("AIzaSyBW5UaZZJgkHLS5WGvr3R6kUsy4vea3xcE")
    private val LOCATION_PERMISSION_REQUEST_CODE = 100

    lateinit var viewModel: MainViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        mapView = findViewById(R.id.mapView)

        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) { style ->

            viewModel.statuses.observe(this) { statuses ->
                val geoJsonString = assets.open("polygons.geojson")
                    .bufferedReader()
                    .use { it.readText() }

                PolygonColorUtils.applyPolygonColors(geoJsonString, statuses, style)
            }

            // Начинаем загрузку статусов
            lifecycleScope.launch {
                viewModel.fetchStatuses(googleSheetsService)
            }
        }
        enableLocationComponent()

        findViewById<Button>(R.id.location_button).setOnClickListener {
            // Получаем плагин для работы с локацией
            val locationComponentPlugin = mapView.location

            // Слушатель для отслеживания изменений местоположения
            locationComponentPlugin.addOnIndicatorPositionChangedListener(object :
                OnIndicatorPositionChangedListener {
                override fun onIndicatorPositionChanged(point: Point) {
                    // Как только местоположение обновится, установим позицию камеры
                    mapView.getMapboxMap().setCamera(
                        CameraOptions.Builder()
                            .center(point) // Устанавливаем центр карты на текущее местоположение
                            .zoom(14.0) // Задаем уровень зума
                            .build()
                    )

                    // Убираем слушатель, чтобы не отслеживать местоположение постоянно
                    locationComponentPlugin.removeOnIndicatorPositionChangedListener(this)
                }
            })
        }
    }

    // Method to enable the location component and display user location
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
//        locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
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