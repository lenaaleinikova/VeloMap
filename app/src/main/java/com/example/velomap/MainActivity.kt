package com.example.velomap

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.fillLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource

class MainActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var style: Style


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.mapView)

        // Загрузка стиля карты
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) { loadedStyle ->
            // Сохраняем загруженный стиль в переменную
            style = loadedStyle

            // После загрузки стиля вызываем метод для отображения GeoJSON полигонов
            loadGeoJson(style)
        }
    }
    private fun loadGeoJson(style: Style) {
        try {
            val geoJsonString = assets.open("polygons.geojson").bufferedReader().use { it.readText() }
            Log.d("GeoJSON", "Файл успешно загружен")

            val geoJsonSource = geoJsonSource("polygon-source") {
                data(geoJsonString)
            }

            // Добавляем источник на карту
            style.addSource(geoJsonSource)

            // Добавляем слой для отображения полигона
            style.addLayer(
                fillLayer("polygon-layer", "polygon-source") {
                    fillColor("#ff0000") // Цвет полигона
                    fillOpacity(0.5)    // Прозрачность полигона
                }
            )
            Log.d("GeoJSON", "слой для отображения полигона")
        } catch (e: Exception) {
            Log.e("GeoJSON", "Ошибка при загрузке файла: ${e.message}")
        }
        // Создаем GeoJsonSource

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