package com.example.velomap

import android.view.View
import android.widget.TextView
import com.example.velomap.data.PolygonInfo

class PolygonInfoViewBinder(private val view: View) {
    fun bind(polygonInfo: PolygonInfo) {
        view.findViewById<TextView>(R.id.textViewId).text = "ID: ${polygonInfo.id}"
        view.findViewById<TextView>(R.id.textViewStatus).text = "Статус: ${polygonInfo.status}"
        view.findViewById<TextView>(R.id.textViewOperator).text = "Оператор: ${polygonInfo.operator}"
    }
}