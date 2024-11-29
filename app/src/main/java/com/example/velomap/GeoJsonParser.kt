package com.example.velomap

import android.util.Log
import com.example.velomap.data.PolygonData
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.MultiPolygon
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


suspend fun parseGeoJson(geoJsonString: String): List<PolygonData> = withContext(Dispatchers.IO){
    val featureCollection = FeatureCollection.fromJson(geoJsonString)
    val polygons = mutableListOf<PolygonData>()

    featureCollection.features()?.forEach { feature ->
        val geometry = feature.geometry()
        if (geometry == null) {
            Log.e("GeoJsonError", "Geometry is null for feature: ${feature.toJson()}")
            return@forEach
        }

        when (geometry) {
            is Polygon -> {

                val coordinates = geometry.coordinates()
                val centroid = calculateCentroid(coordinates)
                val polygonData = PolygonData(
                    id = feature.getStringProperty("iid") ?: "unknown_id",
                    centroid = centroid
                )
                polygons.add(polygonData)
            }
            is MultiPolygon -> {

                val allPolygonsCentroids = geometry.coordinates().map { polygonCoords ->
                    calculateCentroid(polygonCoords)
                }

                val avgLongitude = allPolygonsCentroids.map { it.longitude() }.average()
                val avgLatitude = allPolygonsCentroids.map { it.latitude() }.average()
                val centroid = Point.fromLngLat(avgLongitude, avgLatitude)

                val polygonData = PolygonData(
                    id = feature.getStringProperty("iid") ?: "unknown_id",
                    centroid = centroid
                )
                Log.d("GeoJSON", polygonData.toString())
                polygons.add(polygonData)
            }
            else -> {
                Log.e("GeoJsonError", "Unexpected geometry type: ${geometry.type()}")
            }
        }
    }
    polygons
}


private fun calculateCentroid(coordinates: List<List<Point>>): Point {
    var totalLongitude = 0.0
    var totalLatitude = 0.0
    var pointCount = 0

    for (polygon in coordinates) {
        for (point in polygon) {
            totalLongitude += point.longitude()
            totalLatitude += point.latitude()
            pointCount++
        }
    }

    return Point.fromLngLat(totalLongitude / pointCount, totalLatitude / pointCount)
}