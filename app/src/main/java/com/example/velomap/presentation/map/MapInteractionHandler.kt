package com.example.velomap.presentation.map

import android.widget.Toast
import com.example.velomap.domen.PolygonInfo
import com.mapbox.maps.MapView
import com.mapbox.maps.RenderedQueryGeometry
import com.mapbox.maps.RenderedQueryOptions
import com.mapbox.maps.plugin.gestures.addOnMapClickListener

class MapInteractionHandler(
    private val mapView: MapView,
    private val layerIds: List<String>,
    private val polygons: List<PolygonInfo>,
    private val onPolygonSelected: (PolygonInfo) -> Unit
) {

    init {
        setupMapClickListener()
    }

    private fun setupMapClickListener() {
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
                        val polygonId = properties?.get("iid")?.asString?.replace("\"", "")?.trim()

                        val polygonInfo = polygons.find { polygon -> polygon.id == polygonId }
                        if (polygonInfo != null) {
                            onPolygonSelected(polygonInfo)
                        } else {
                            showToast("Polygon information not found")
                        }
                    }
                } else {
                    showToast("No polygon found")
                }
            }
            true
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(mapView.context, message, Toast.LENGTH_SHORT).show()
    }
}
