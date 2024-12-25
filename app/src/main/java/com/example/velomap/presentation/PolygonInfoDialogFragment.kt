package com.example.velomap.presentation

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.velomap.R
import com.example.velomap.domen.PolygonInfo
import com.example.velomap.data.repository.PolygonRepository
import com.example.velomap.data.network.GoogleSheetsManager
import com.example.velomap.data.network.GoogleSheetsService
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import kotlinx.coroutines.launch

class PolygonInfoDialogFragment : DialogFragment() {

    private lateinit var googleSheetsService: GoogleSheetsService
    private lateinit var googleAccountCredential: GoogleAccountCredential

    private lateinit var googleSheetsManager: GoogleSheetsManager

    private lateinit var viewModel: MainViewModel

    companion object {
        private const val REQUEST_ACCOUNT_PICKER = 1000
        private const val REQUEST_CODE_AUTHORIZATION = 1001
        private const val GET_ACCOUNTS_PERMISSION_REQUEST_CODE = 200

        fun newInstance(polygonInfo: PolygonInfo): PolygonInfoDialogFragment {
            val fragment = PolygonInfoDialogFragment()
            fragment.arguments = Bundle().apply {
                putString("id", polygonInfo.id)
                putString("status", polygonInfo.status)
                putString("operator", polygonInfo.operator)
            }
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_polygon_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        googleSheetsManager = GoogleSheetsManager(requireContext())
        googleAccountCredential = googleSheetsManager.initCredential()

        if (googleAccountCredential.selectedAccountName == null) {
            googleSheetsManager.chooseAccount(this, REQUEST_ACCOUNT_PICKER)
        }

        setupViewModel()
        handlePermissions()

        val polygonInfo = PolygonInfo(
            id = arguments?.getString("id") ?: "",
            status = arguments?.getString("status") ?: "",
            operator = arguments?.getString("operator") ?: ""
        )

        PolygonInfoViewBinder(view).bind(polygonInfo)

        setupListeners(view)
    }

    private fun setupViewModel() {
        val repository = PolygonRepository(
            GoogleSheetsService(
                "AIzaSyBW5UaZZJgkHLS5WGvr3R6kUsy4vea3xcE",
                requireContext()
            )
        )
        viewModel = ViewModelProvider(this, ViewModelFactory(repository))[MainViewModel::class.java]
    }

    private fun handlePermissions() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.GET_ACCOUNTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.GET_ACCOUNTS),
                GET_ACCOUNTS_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun setupListeners(view: View) {
        val statusSpinner = view.findViewById<Spinner>(R.id.status_spinner)
        val updateButton = view.findViewById<Button>(R.id.update_button)
        updateButton.setOnClickListener {
            val polygonId = arguments?.getString("id") ?: return@setOnClickListener
            val selectedStatus = statusSpinner.selectedItem.toString()
            updatePolygonStatus(polygonId, selectedStatus)
        }

        view.findViewById<Button>(R.id.buttonClose).setOnClickListener { dismiss() }
    }

    private fun updatePolygonStatus(polygonId: String, newStatus: String) {
        lifecycleScope.launch {
            try {
                val intent =
                    googleSheetsService.testUpdateCell(googleAccountCredential, requireContext())
                if (intent != null) {
                    startActivityForResult(intent, REQUEST_CODE_AUTHORIZATION)
                } else {
                    Log.d("PolygonInfoDialog", "Status updated successfully")
                    Toast.makeText(requireContext(), "Статус успешно обновлен", Toast.LENGTH_SHORT)
                        .show()
                    dismiss()
                }
            } catch (e: Exception) {
                Log.e("PolygonInfoDialog", "Error updating status", e)
                Toast.makeText(requireContext(), "Не удалось обновить статус", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ACCOUNT_PICKER && resultCode == RESULT_OK && data != null) {
            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)?.let {
                googleAccountCredential.selectedAccountName = it
                googleSheetsManager.saveAccountName(it)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == GET_ACCOUNTS_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            googleSheetsManager.getSavedAccount()
        } else {
            Toast.makeText(
                requireContext(),
                "Permission denied to access accounts",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

}
