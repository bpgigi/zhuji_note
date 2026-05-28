package com.zhuji.note.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

interface SecretStore {
    fun getDeepSeekKey(): String
    fun setDeepSeekKey(value: String)
    fun clear()
}

class EncryptedSecretStore(private val context: Context) : SecretStore {

    private val prefs: SharedPreferences by lazy {
        runCatching {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                "zhuji_secrets",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        }.getOrElse {
            context.getSharedPreferences("zhuji_secrets_fb", Context.MODE_PRIVATE)
        }
    }

    override fun getDeepSeekKey(): String = prefs.getString("ds_key", "").orEmpty()

    override fun setDeepSeekKey(value: String) {
        prefs.edit().putString("ds_key", value.trim()).apply()
    }

    override fun clear() {
        prefs.edit().clear().apply()
    }
}
