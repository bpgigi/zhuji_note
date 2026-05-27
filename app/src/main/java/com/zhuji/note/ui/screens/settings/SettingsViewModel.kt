package com.zhuji.note.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuji.note.ai.DeepSeekClient
import com.zhuji.note.data.local.preferences.ThemeMode
import com.zhuji.note.data.local.preferences.UserPreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val theme: ThemeMode = ThemeMode.System,
    val dynamicColor: Boolean = false,
    val accentNeon: Boolean = false,
    val keyDraft: String = "",
    val model: String = "deepseek-v4-flash",
    val modelOptions: List<String> = listOf("deepseek-v4-flash", "deepseek-v4-pro"),
    val balanceText: String = "",
    val errorMessage: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: UserPreferencesDataStore,
    private val ai: DeepSeekClient,
) : ViewModel() {

    private val extraState = MutableStateFlow(SettingsUiState())

    val state: StateFlow<SettingsUiState> = combine(prefs.flow, extraState) { p, extra ->
        extra.copy(
            theme = p.theme,
            dynamicColor = p.dynamicColor,
            accentNeon = p.accentNeon,
            keyDraft = if (extra.keyDraft.isBlank()) p.deepseekKey else extra.keyDraft,
            model = p.deepseekModel,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setTheme(t: ThemeMode) = viewModelScope.launch { prefs.setTheme(t) }
    fun setDynamicColor(on: Boolean) = viewModelScope.launch { prefs.setDynamic(on) }
    fun setNeon(on: Boolean) = viewModelScope.launch { prefs.setNeon(on) }
    fun setModel(m: String) = viewModelScope.launch { prefs.setModel(m) }

    fun saveKey(key: String) = viewModelScope.launch {
        prefs.setKey(key)
        extraState.update { it.copy(keyDraft = key, errorMessage = null) }
    }

    fun refreshModels() = viewModelScope.launch {
        runCatching { ai.listModels(prefs.flow.first().deepseekKey) }
            .onSuccess { list ->
                if (list.isNotEmpty()) {
                    extraState.update { it.copy(modelOptions = list.map { m -> m.id }, errorMessage = null) }
                }
            }
            .onFailure { t -> extraState.update { it.copy(errorMessage = "模型列表错误：${t.message}") } }
    }

    fun refreshBalance() = viewModelScope.launch {
        runCatching { ai.balance(prefs.flow.first().deepseekKey) }
            .onSuccess { b ->
                if (b == null) {
                    extraState.update { it.copy(balanceText = "") }
                } else {
                    val info = b.infos.firstOrNull()
                    val txt = info?.let { "${it.currency} ${it.total}" } ?: "已就绪"
                    extraState.update { it.copy(balanceText = txt, errorMessage = null) }
                }
            }
            .onFailure { t -> extraState.update { it.copy(errorMessage = "余额错误：${t.message}") } }
    }
}
