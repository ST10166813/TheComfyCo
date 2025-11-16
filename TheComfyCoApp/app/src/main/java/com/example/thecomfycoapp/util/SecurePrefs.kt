// File: SecurePrefs.kt
package com.example.thecomfycoapp.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object SecurePrefs {
    private const val NAME = "secure_prefs"

    fun getEncryptedPrefs(context: Context) =
        EncryptedSharedPreferences.create(
            NAME,
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            context.applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
}
