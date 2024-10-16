package com.example.velomap


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


object PolygonColorUtils {
    fun applyPolygonColors(
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
                minZoom(13.0)
            }
        )
    }
}
