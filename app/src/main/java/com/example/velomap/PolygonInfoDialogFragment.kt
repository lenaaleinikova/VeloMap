package com.example.velomap

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch


class PolygonInfoDialogFragment : DialogFragment() {

    private lateinit var googleSheetsService: GoogleSheetsService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.dialog_polygon_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Инициализация googleSheetsService
        googleSheetsService = GoogleSheetsService("AIzaSyBW5UaZZJgkHLS5WGvr3R6kUsy4vea3xcE", requireContext())

        val polygonId = arguments?.getString("id")
        val polygonStatus = arguments?.getString("status")
        val polygonOperator = arguments?.getString("operator")

        view.findViewById<TextView>(R.id.textViewId).text = "ID: $polygonId"
        view.findViewById<TextView>(R.id.textViewStatus).text = "Статус: $polygonStatus"
        view.findViewById<TextView>(R.id.textViewOperator).text = "Оператор: $polygonOperator"

        val statusSpinner = view.findViewById<Spinner>(R.id.status_spinner)
        val updateBottom = view.findViewById<Button>(R.id.update_button)
        updateBottom.setOnClickListener{
            val selectedStatus = statusSpinner.selectedItem.toString()
            updatePolygonStatus(polygonId.toString(), selectedStatus)
        }

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
    private fun updatePolygonStatus(polygonId: String, newStatus: String) {
        lifecycleScope.launch {
            try {
                // Используем Google Sheets service для обновления статуса
//                googleSheetsService.updateStatus(polygonId, newStatus)
                googleSheetsService.testUpdateCell()
                Toast.makeText(requireContext(), "Статус успешно обновлен", Toast.LENGTH_SHORT).show()
                dismiss()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Не удалось обновить статус", Toast.LENGTH_SHORT).show()
                Log.e("PolygonInfoDialog", "Ошибка обновления статуса", e)
            }
        }
    }


}
