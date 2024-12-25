package com.example.velomap.data.network

import android.content.Context
import androidx.fragment.app.Fragment
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.sheets.v4.SheetsScopes

class GoogleSheetsManager(
    private val context: Context
) {
    private val credential: GoogleAccountCredential = GoogleAccountCredential.usingOAuth2(
        context,
        listOf(SheetsScopes.SPREADSHEETS)
    )

    fun chooseAccount(fragment: Fragment, requestCode: Int) {
        val intent = credential.newChooseAccountIntent()
        fragment.startActivityForResult(intent, requestCode)
    }

    fun initCredential(): GoogleAccountCredential {
        credential.selectedAccountName = getSavedAccount()
        return credential
    }

    fun saveAccountName(accountName: String) {
        val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().putString("accountName", accountName).apply()
    }

    fun getSavedAccount(): String? {
        val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("accountName", null)
    }
}