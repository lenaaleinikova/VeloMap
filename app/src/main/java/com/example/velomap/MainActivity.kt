package com.example.velomap

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor
import com.mapbox.maps.extension.style.layers.properties.generated.TextJustify
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var style: Style

    private val googleSheetsService = GoogleSheetsService("AIzaSyBW5UaZZJgkHLS5WGvr3R6kUsy4vea3xcE")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.mapView)

//        // Загрузка стиля карты
//        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) { loadedStyle ->
//            // Сохраняем загруженный стиль в переменную
//            style = loadedStyle
//
//            // После загрузки стиля вызываем метод для отображения GeoJSON полигонов
//            loadGeoJson(style)
//        }
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) { style ->
            lifecycleScope.launch {
                val statuses = googleSheetsService.fetchStatuses() // Получаем данные из Google Sheets
                val geoJsonString = assets.open("polygons.geojson").bufferedReader().use { it.readText() }

                applyPolygonColors(geoJsonString, statuses, style) // Раскрашиваем полигоны
            }
        }
    }

    private fun loadGeoJson(style: Style) {
        try {
            val geoJsonString =
                assets.open("polygons.geojson").bufferedReader().use { it.readText() }
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

            // Добавляем SymbolLayer для отображения текста (названий) на полигонах
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

            Log.d("GeoJSON", "Слой для отображения названий полигонов добавлен")
        } catch (e: Exception) {
            Log.e("GeoJSON", "Ошибка при загрузке файла: ${e.message}")
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

        statuses.forEach { (iid, status) ->
            val color = when (status) {
                "1" -> "#00FF00" // Зеленый
                "принято" -> "#0000FF" // Голубой
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