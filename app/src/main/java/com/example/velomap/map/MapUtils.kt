package com.example.velomap.map

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.velomap.R
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.interpolate

object MapUtils {
    fun enableLocationComponent(activity: AppCompatActivity, mapView: MapView) {
        if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED) {
            initLocationComponent(mapView)
        } else {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                100 // LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    fun initLocationComponent(mapView: MapView) {
        val locationComponentPlugin = mapView.location
        locationComponentPlugin.updateSettings {
            puckBearing = PuckBearing.COURSE
            puckBearingEnabled = true
            enabled = true
            locationPuck = LocationPuck2D(
                bearingImage = ImageHolder.from(R.drawable.circle_red),
                shadowImage = ImageHolder.from(R.drawable.triangle_1),
                scaleExpression = interpolate {
                    linear()
                    zoom()
                    stop(0.0, 0.6)
                    stop(20.0, 1.0)
                }.toJson()
            )
        }
    }
}
