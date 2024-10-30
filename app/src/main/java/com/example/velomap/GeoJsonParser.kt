package com.example.velomap

import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon

// Ваша функция для парсинга GeoJSON
fun parseGeoJson(geoJsonString: String): List<PolygonData> {
    val featureCollection = FeatureCollection.fromJson(geoJsonString)
    val polygons = mutableListOf<PolygonData>() // Список для хранения данных о полигонах

    featureCollection.features()?.forEach { feature ->
        val geometry = feature.geometry()
        if (geometry is Polygon) {
            val coordinates = geometry.coordinates()
            val centroid = calculateCentroid(coordinates)
            val polygonData = PolygonData(
                id = feature.getStringProperty("iid"), // ID полигона
                centroid = centroid // Центроид полигона
            )
            polygons.add(polygonData)
        }
    }
    return polygons
}

// Функция для вычисления центроида
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