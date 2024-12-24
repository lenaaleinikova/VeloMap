package com.example.velomap.map


import com.example.velomap.data.PolygonInfo
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
//        statuses: List<Pair<String, String>>,
        statuses: List<PolygonInfo>,
        style: Style
    ): List<String> { // Теперь возвращаем список названий слоев
        val geoJsonSource = geoJsonSource("polygon-source") {
            data(geoJson)
        }

        style.addSource(geoJsonSource)

        val layerIds = mutableListOf<String>()

        statuses.forEach { polygonInfo ->
            val iid = polygonInfo.id
            val status = polygonInfo.status

            val color = when (status) {
                "1" -> "#00FF00"
                "Принято" -> "#0000FF"
                "не снято" -> "#FF0000"
                else -> "#800080"
            }

            val fillLayerId = "polygon-layer-$iid"
            style.addLayer(
                fillLayer(fillLayerId, "polygon-source") {
                    filter(eq(get("iid"), literal(iid)))
                    fillColor(color)
                    fillOpacity(0.5)
                }
            )
            layerIds.add(fillLayerId)

            style.addLayer(
                lineLayer("polygon-outline-layer-$iid", "polygon-source") {
                    filter(eq(get("iid"), literal(iid)))
                    lineColor("#000000")
                    lineWidth(1.0)
                }
            )
        }

        style.addLayer(
            symbolLayer("polygon-label-layer", "polygon-source") {
                textField("{iid}")
                textSize(14.0)
                textColor("#000000")
                textJustify(TextJustify.CENTER)
                textAnchor(TextAnchor.CENTER)
                textIgnorePlacement(true)
                textAllowOverlap(true)
                minZoom(13.0)
            }
        )

        return layerIds
    }
}
