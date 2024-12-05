package com.example.velomap

import android.accounts.AccountManager
import android.app.Activity
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.velomap.data.PolygonInfo
import com.example.velomap.network.GoogleSheetsService
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.sheets.v4.SheetsScopes
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager


class PolygonInfoDialogFragment : DialogFragment() {

    private lateinit var googleSheetsService: GoogleSheetsService
    private lateinit var googleAccountCredential: GoogleAccountCredential
    private val REQUEST_ACCOUNT_PICKER = 1000
    private val REQUEST_CODE_AUTHORIZATION = 1001

    private val GET_ACCOUNTS_PERMISSION_REQUEST_CODE = 200

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

//        val accountName = viewModel.accountName.value.toString()
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED) {
            // Разрешение предоставлено, можно работать с учетными записями
            getSavedAccount()
        } else {
            // Разрешение не предоставлено, запрашиваем его
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.GET_ACCOUNTS), GET_ACCOUNTS_PERMISSION_REQUEST_CODE)
        }

        googleSheetsService =
            GoogleSheetsService("AIzaSyBW5UaZZJgkHLS5WGvr3R6kUsy4vea3xcE", requireContext())

        googleAccountCredential = GoogleAccountCredential.usingOAuth2(
            requireContext(),
            listOf(SheetsScopes.SPREADSHEETS)
        )

        val accountName = getSavedAccount()
        Log.d("testUpdate", "0 $accountName")
        googleAccountCredential.selectedAccountName = accountName.toString()
//        val name = getSavedAccount()
//        Log.d("testUpdate","11 "+name)


        if (googleAccountCredential.selectedAccountName == null) {
            chooseAccount()
            Log.d("testUpdate","1 "+ googleAccountCredential.selectedAccountName)
        } else Log.d("testUpdate", googleAccountCredential.selectedAccountName)
//        getSavedAccount()
        val savedAccount = getSavedAccount()
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
                updatePolygonStatus(polygonId.toString(), selectedStatus, requireContext())
            } catch (e: Exception) {
                Log.d("Dialog", e.toString())
            }

        }

        view.findViewById<Button>(R.id.buttonClose).setOnClickListener {
            dismiss()
        }
    }



    private fun updatePolygonStatus(polygonId: String, newStatus: String, context: Context) {
        lifecycleScope.launch {
            try {
                Log.d("testUpdate", googleAccountCredential.selectedAccountName)
//                googleSheetsService.updateStatus(polygonId, newStatus)
                val intent = googleSheetsService.testUpdateCell(googleAccountCredential, context)
                if (intent != null) {
                    startActivityForResult(intent, REQUEST_CODE_AUTHORIZATION)
                } else {
                    Log.d("testUpdate", "Данные успешно обновлены!")
                }
                Toast.makeText(requireContext(), "Статус успешно обновлен", Toast.LENGTH_SHORT)
                    .show()
                dismiss()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Не удалось обновить статус", Toast.LENGTH_SHORT)
                    .show()
                Log.e("testUpdate", "Ошибка обновления статуса", e)
            }
        }
    }

    private fun chooseAccount() {
        val intent = googleAccountCredential.newChooseAccountIntent()
        startActivityForResult(intent, REQUEST_ACCOUNT_PICKER)
        val accountName = googleAccountCredential.selectedAccountName
        if (accountName != null) {
            saveAccountNameToPreferences(accountName)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ACCOUNT_PICKER && resultCode == RESULT_OK && data != null) {
            val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            if (accountName != null) {
                googleAccountCredential.selectedAccountName = accountName
                saveAccountNameToPreferences(accountName)
            }

//            if (requestCode == REQUEST_CODE_AUTHORIZATION) {
//                if (resultCode == Activity.RESULT_OK) {
//                    // Повторите операцию после получения разрешения
//                    updatePolygonStatus()
//                } else {
//                    // Пользователь отклонил запрос
//                    Log.e("YourFragment", "Пользователь отклонил разрешение")
//                }
//            }
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == GET_ACCOUNTS_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешение получено, можно работать с учетными записями
                getSavedAccount()
            } else {
                // Разрешение не получено, информируем пользователя
                Toast.makeText(requireContext(), "Permission denied to access accounts", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveAccountNameToPreferences(accountName: String) {
        val sharedPref = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().putString("accountName", accountName).apply()
    }
    private fun getSavedAccount(): String? {
//        val sharedPreferences =
//            requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val sharedPreferences = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("accountName", null)
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
