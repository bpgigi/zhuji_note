package com.zhuji.note.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "zhuji_prefs")

enum class ThemeMode { System, Light, Dark }

data class UserPrefs(
    val theme: ThemeMode = ThemeMode.System,
    val dynamicColor: Boolean = false,
    val deepseekKey: String = "",
    val deepseekModel: String = "deepseek-v4-flash",
    val font: String = "Inter",
    val accentNeon: Boolean = false,
    val firstRun: Boolean = true,
)

class UserPreferencesDataStore(private val context: Context) {

    private val K_THEME = stringPreferencesKey("theme")
    private val K_DYNAMIC = booleanPreferencesKey("dynamic_color")
    private val K_KEY = stringPreferencesKey("deepseek_key")
    private val K_MODEL = stringPreferencesKey("deepseek_model")
    private val K_FONT = stringPreferencesKey("font")
    private val K_NEON = booleanPreferencesKey("accent_neon")
    private val K_FIRST = booleanPreferencesKey("first_run")
    private val K_FONT_SCALE = intPreferencesKey("font_scale")

    val flow: Flow<UserPrefs> = context.dataStore.data.map { p ->
        UserPrefs(
            theme = runCatching { ThemeMode.valueOf(p[K_THEME] ?: ThemeMode.System.name) }.getOrDefault(ThemeMode.System),
            dynamicColor = p[K_DYNAMIC] ?: false,
            deepseekKey = p[K_KEY] ?: "",
            deepseekModel = p[K_MODEL] ?: "deepseek-v4-flash",
            font = p[K_FONT] ?: "Inter",
            accentNeon = p[K_NEON] ?: false,
            firstRun = p[K_FIRST] ?: true,
        )
    }

    val fontScale: Flow<Int> = context.dataStore.data.map { it[K_FONT_SCALE] ?: 0 }

    suspend fun setTheme(mode: ThemeMode) = context.dataStore.edit { it[K_THEME] = mode.name }
    suspend fun setDynamic(on: Boolean) = context.dataStore.edit { it[K_DYNAMIC] = on }
    suspend fun setNeon(on: Boolean) = context.dataStore.edit { it[K_NEON] = on }
    suspend fun setKey(key: String) = context.dataStore.edit { it[K_KEY] = key.trim() }
    suspend fun setModel(model: String) = context.dataStore.edit { it[K_MODEL] = model }
    suspend fun setFont(font: String) = context.dataStore.edit { it[K_FONT] = font }
    suspend fun setFontScale(delta: Int) = context.dataStore.edit { it[K_FONT_SCALE] = delta }
    suspend fun consumeFirstRun() = context.dataStore.edit { it[K_FIRST] = false }
}
