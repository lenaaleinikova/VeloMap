package com.example.velomap.presentation


import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.velomap.R
import com.example.velomap.domen.PolygonData
import com.example.velomap.data.repository.PolygonRepository
import com.example.velomap.data.parseGeoJson
import com.example.velomap.data.network.GeoJsonManager
import com.example.velomap.data.network.GoogleSheetsService
import com.example.velomap.presentation.map.MapInteractionHandler
import com.example.velomap.presentation.map.MapManager
import com.example.velomap.presentation.map.MapUtils
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.sheets.v4.SheetsScopes
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var layerIds: List<String>
    private lateinit var polygonsList: List<PolygonData>

    private lateinit var viewModel: MainViewModel
    private lateinit var mapManager: MapManager
    private lateinit var searchHandler: SearchHandler

    private val LOCATION_PERMISSION_REQUEST_CODE = 100
    private lateinit var googleAccountCredential: GoogleAccountCredential


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        mapView = findViewById(R.id.mapView)
        val searchButton = findViewById<ImageButton>(R.id.search_button)

        searchButton.setOnClickListener {
            Log.d("Search", "Button clicked1")
        }

        googleAccountCredential = GoogleAccountCredential.usingOAuth2(
            this,
            listOf(SheetsScopes.SPREADSHEETS)
        )

        val repository =
            PolygonRepository(GoogleSheetsService("AIzaSyBW5UaZZJgkHLS5WGvr3R6kUsy4vea3xcE", this))
        viewModel = ViewModelProvider(this, ViewModelFactory(repository))[MainViewModel::class.java]
        mapManager = MapManager(mapView, this)

        searchHandler = SearchHandler(this, ::onPolygonFound)

        Log.d("Mainload", "load")

        observeViewModel(this)
        viewModel.fetchPolygonInfo()


        MapUtils.enableLocationComponent(this, mapView)

        findViewById<ImageButton>(R.id.location_button).setOnClickListener {
            val locationComponentPlugin = mapView.location
            locationComponentPlugin.addOnIndicatorPositionChangedListener(object :
                OnIndicatorPositionChangedListener {
                override fun onIndicatorPositionChanged(point: Point) {
                    mapView.getMapboxMap().setCamera(
                        CameraOptions.Builder()
                            .center(point)
                            .zoom(14.0)
                            .build()
                    )
                    locationComponentPlugin.removeOnIndicatorPositionChangedListener(this)
                }
            })
        }
    }

    private fun observeViewModel(context: Context) {
        Log.d("Mainload", "observeViewModel")
        viewModel.polygonInfo.observe(this) { result ->
            Log.d("Mainload", "result ${result.toString()}")
            result.onSuccess { polygons ->
                val geoJsonUrl =
                    "https://geoserver.helgilab.ru/geoserver/SURV/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=SURV%3Avelo2024_3part_2&outputFormat=application%2Fjson"

                lifecycleScope.launch {
                    try {

                        val geoJsonString = GeoJsonManager(context).fetchGeoJson((geoJsonUrl))
                        Log.d("GeoJson", "geoJsonString $geoJsonString")

                        if (geoJsonString != null) {
                            polygonsList = parseGeoJson(geoJsonString)
                            Log.d("GeoJson", "polygons: $polygonsList")
                            layerIds = mapManager.loadStyle(polygons, geoJsonString)
                            Log.d("GeoJson", "layerIds $layerIds")
                            searchHandler.setPolygons(polygonsList)
                            MapInteractionHandler(mapView, layerIds, polygons) { polygonInfo ->
                                val dialog = PolygonInfoDialogFragment.newInstance(polygonInfo)
                                dialog.show(supportFragmentManager, "PolygonInfoDialog")
                            }
                        } else {
                            Log.e("GeoJsonError", "GeoJSON data is null")
                        }
                    } catch (e: Exception) {
                        Log.e("GeoJsonError", "Error loading GeoJSON data", e)
                    }
                }
            }
            result.onFailure {
                Toast.makeText(this, "Failed to load polygon info", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onPolygonFound(polygon: PolygonData) {
        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .center(polygon.centroid)
                .zoom(16.0)
                .build()
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            MapUtils.initLocationComponent(mapView)
        } else {
            Toast.makeText(this, "Location permission not granted!!!", Toast.LENGTH_SHORT).show()
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