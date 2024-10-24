package com.example.velomap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment


class PolygonInfoDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.dialog_polygon_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val polygonId = arguments?.getString("id")
        val polygonStatus = arguments?.getString("status")
        val polygonOperator = arguments?.getString("operator")

        view.findViewById<TextView>(R.id.textViewId).text = "ID: $polygonId"
        view.findViewById<TextView>(R.id.textViewStatus).text = "Статус: $polygonStatus"
        view.findViewById<TextView>(R.id.textViewOperator).text = "Оператор: $polygonOperator"

        view.findViewById<Button>(R.id.buttonClose).setOnClickListener {
            dismiss() // Закрываем диалог по нажатию на кнопку
        }
    }

    companion object {
        fun newInstance(polygonInfo: PolygonInfo): PolygonInfoDialogFragment {
            val fragment = PolygonInfoDialogFragment()
            val args = Bundle().apply {
                putString("id", polygonInfo.id)
                putString("status", polygonInfo.status)
                putString("operator", polygonInfo.operator)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
