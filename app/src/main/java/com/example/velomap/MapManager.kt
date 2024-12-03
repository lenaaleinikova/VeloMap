package com.example.velomap

import androidx.appcompat.app.AppCompatActivity
import com.example.velomap.data.PolygonInfo
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.plugin.locationcomponent.location

class MapManager(private val mapView: MapView, private val activity: AppCompatActivity) {

    fun loadStyle(
        polygonInfo: List<PolygonInfo>,
        geoJsonString: String
    ): List<String> {
        val layerIds = PolygonColorUtils.applyPolygonColors(geoJsonString, polygonInfo, mapView.getMapboxMap().getStyle()!!)
        return layerIds
    }

    fun zoomToPolygon(centroid: Point) {
        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .center(centroid)
                .zoom(16.0)
                .build()
        )
    }

    fun enableLocationComponent() {
        val locationComponentPlugin = mapView.location
        locationComponentPlugin.updateSettings {
            enabled = true
            locationPuck = LocationPuck2D()
        }
    }
}
