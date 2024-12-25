package com.example.velomap.presentation

import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.velomap.R
import com.example.velomap.domain.PolygonData

class SearchHandler(
    private val activity: AppCompatActivity,
    private val onPolygonFound: (PolygonData) -> Unit
) {
    private val searchInput: EditText = activity.findViewById(R.id.polygon_id_input)
    private val searchButton: ImageButton = activity.findViewById(R.id.search_button)

    private var polygons: List<PolygonData> = emptyList()

    init {
        searchButton.setOnClickListener { handleSearch() }
    }

    fun setPolygons(polygons: List<PolygonData>) {
        this.polygons = polygons
    }

    fun handleSearch() {
        val polygonId = searchInput.text.toString().trim()
        if (polygonId.isEmpty()) {
            showToast("Введите ID полигона")
            return
        }

        val polygon = polygons.find { it.id == polygonId }
        if (polygon != null) {
            onPolygonFound(polygon)
        } else {
            showToast("Полигон с ID $polygonId не найден")
        }

        searchInput.text.clear()
    }

    private fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }
}