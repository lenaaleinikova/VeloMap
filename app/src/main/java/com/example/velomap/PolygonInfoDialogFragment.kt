package com.example.velomap

import android.accounts.AccountManager
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.velomap.data.PolygonInfo
import com.example.velomap.network.GoogleSheetsService
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.sheets.v4.SheetsScopes
import kotlinx.coroutines.launch


class PolygonInfoDialogFragment : DialogFragment() {

    private lateinit var googleSheetsService: GoogleSheetsService
    private lateinit var googleAccountCredential: GoogleAccountCredential
    private val REQUEST_ACCOUNT_PICKER = 1000

    private lateinit var viewModel: MainViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.dialog_polygon_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository =
            PolygonRepository(GoogleSheetsService("AIzaSyBW5UaZZJgkHLS5WGvr3R6kUsy4vea3xcE", requireContext()))
        viewModel = ViewModelProvider(this, ViewModelFactory(repository))[MainViewModel::class.java]

        val accountName = viewModel.accountName.value.toString()
        Log.d("testUpdate", "0 $accountName")

        googleSheetsService =
            GoogleSheetsService("AIzaSyBW5UaZZJgkHLS5WGvr3R6kUsy4vea3xcE", requireContext())

        googleAccountCredential = GoogleAccountCredential.usingOAuth2(
            requireContext(),
            listOf(SheetsScopes.SPREADSHEETS)
        )
        googleAccountCredential.selectedAccountName = accountName.toString()
        val name = getSavedAccount()
        Log.d("testUpdate","11 "+name)
        if (googleAccountCredential.selectedAccountName == null) {
//            chooseAccount()
            Log.d("testUpdate","1 "+ googleAccountCredential.selectedAccountName)
        } else Log.d("testUpdate", googleAccountCredential.selectedAccountName)

//        val savedAccount = getSavedAccount()
////
//        if (savedAccount != null) {
//            googleAccountCredential.selectedAccountName = savedAccount
//        } else {
//            promptAccountSelection()
//        }


        val polygonId = arguments?.getString("id")
        val polygonStatus = arguments?.getString("status")
        val polygonOperator = arguments?.getString("operator")

        view.findViewById<TextView>(R.id.textViewId).text = "ID: $polygonId"
        view.findViewById<TextView>(R.id.textViewStatus).text = "Статус: $polygonStatus"
        view.findViewById<TextView>(R.id.textViewOperator).text = "Оператор: $polygonOperator"

        val statusSpinner = view.findViewById<Spinner>(R.id.status_spinner)
        val updateBottom = view.findViewById<Button>(R.id.update_button)
        updateBottom.setOnClickListener {
            val selectedStatus = statusSpinner.selectedItem.toString()
            try {
                updatePolygonStatus(polygonId.toString(), selectedStatus)
            } catch (e: Exception) {
                Log.d("Dialog", e.toString())
            }

        }

        view.findViewById<Button>(R.id.buttonClose).setOnClickListener {
            dismiss()
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
                Log.d("testUpdate", googleAccountCredential.selectedAccountName)
//                googleSheetsService.updateStatus(polygonId, newStatus)
//                googleSheetsService.testUpdateCell()
//                googleSheetsService.chooseAccount(googleAccountCredential)
                Toast.makeText(requireContext(), "Статус успешно обновлен", Toast.LENGTH_SHORT)
                    .show()
                dismiss()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Не удалось обновить статус", Toast.LENGTH_SHORT)
                    .show()
                Log.e("Dialog", "Ошибка обновления статуса", e)
            }
        }
    }

    private fun chooseAccount() {
        val intent = googleAccountCredential.newChooseAccountIntent()
        startActivityForResult(intent, REQUEST_ACCOUNT_PICKER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ACCOUNT_PICKER && resultCode == RESULT_OK && data != null) {
            val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            if (accountName != null) {
                googleAccountCredential.selectedAccountName = accountName
                saveAccountNameToPreferences(accountName)
            }
        }
    }
    private fun ensureAccountSelected() {
        if (googleAccountCredential.selectedAccountName == null) {
            val savedAccount = getSavedAccount()
            if (savedAccount != null) {
                googleAccountCredential.selectedAccountName = savedAccount
            } else {
                promptAccountSelection()
            }
        }
    }


    private fun saveAccountNameToPreferences(accountName: String) {
        val sharedPref = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().putString("accountName", accountName).apply()
    }

    private fun promptAccountSelection() {
        val accounts = AccountManager.get(requireContext()).getAccountsByType("com.google")
        val accountNames = accounts.map { it.name }.toTypedArray()

        if (accountNames.isNotEmpty()) {
            AlertDialog.Builder(requireContext())
                .setTitle("Выберите аккаунт")
                .setItems(accountNames) { _, which ->
                    val selectedAccount = accountNames[which]
                    saveSelectedAccount(selectedAccount)
                    googleAccountCredential.selectedAccountName = selectedAccount
                }
                .create()
                .show()
        } else {
            Toast.makeText(requireContext(), "Нет доступных аккаунтов Google", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun saveSelectedAccount(accountName: String) {
        val sharedPreferences =
            requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("selectedAccount", accountName).apply()
    }

    private fun getSavedAccount(): String? {
        val sharedPreferences =
            requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("selectedAccount", null)
    }


}
