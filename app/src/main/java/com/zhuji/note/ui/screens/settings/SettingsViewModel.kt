package com.zhuji.note.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuji.note.ai.DeepSeekClient
import com.zhuji.note.data.local.preferences.ThemeMode
import com.zhuji.note.data.local.preferences.UserPreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    val savedKey: String = "",
    val model: String = "deepseek-v4-flash",
    val modelOptions: List<String> = listOf("deepseek-v4-flash", "deepseek-v4-pro"),
    val balanceText: String = "",
    val refreshingBalance: Boolean = false,
    val refreshingModels: Boolean = false,
    val verifying: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: UserPreferencesDataStore,
    private val ai: DeepSeekClient,
) : ViewModel() {

    private val extraState = MutableStateFlow(SettingsUiState())
    private val toasts = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 4)
    val toastsFlow: SharedFlow<String> = toasts.asSharedFlow()

    val state: StateFlow<SettingsUiState> = combine(prefs.flow, extraState) { p, extra ->
        extra.copy(
            theme = p.theme,
            dynamicColor = p.dynamicColor,
            accentNeon = p.accentNeon,
            savedKey = p.deepseekKey,
            keyDraft = if (extra.keyDraft.isBlank()) p.deepseekKey else extra.keyDraft,
            model = p.deepseekModel,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setTheme(t: ThemeMode) = viewModelScope.launch {
        prefs.setTheme(t)
        toasts.emit("主题已切换为 ${t.label()}")
    }
    fun setDynamicColor(on: Boolean) = viewModelScope.launch {
        prefs.setDynamic(on); toasts.emit(if (on) "已启用动态取色" else "已关闭动态取色")
    }
    fun setNeon(on: Boolean) = viewModelScope.launch {
        prefs.setNeon(on); toasts.emit(if (on) "已启用霓虹强调" else "已关闭霓虹强调")
    }
    fun setModel(m: String) = viewModelScope.launch {
        prefs.setModel(m); toasts.emit("模型切换为 $m")
    }

    fun saveKeyDraft(key: String) {
        extraState.update { it.copy(keyDraft = key) }
    }

    fun saveAndVerifyKey(key: String) = viewModelScope.launch {
        if (key.isBlank()) {
            toasts.emit("请输入 API Key")
            return@launch
        }
        extraState.update { it.copy(verifying = true, errorMessage = null, keyDraft = key) }
        prefs.setKey(key)
        runCatching { ai.balance(key) }
            .onSuccess { b ->
                if (b == null) {
                    toasts.emit("Key 已保存（暂未取到余额信息）")
                } else {
                    val info = b.infos.firstOrNull()
                    val txt = info?.let { "${it.currency} ${it.total}" } ?: "已就绪"
                    extraState.update { it.copy(balanceText = txt) }
                    toasts.emit("Key 验证成功：$txt")
                }
            }
            .onFailure { t ->
                extraState.update { it.copy(errorMessage = "验证失败：${t.message}") }
                toasts.emit("验证失败：${t.message}")
            }
        extraState.update { it.copy(verifying = false) }
    }

    fun refreshModels() = viewModelScope.launch {
        extraState.update { it.copy(refreshingModels = true, errorMessage = null) }
        val key = prefs.flow.first().deepseekKey
        runCatching { ai.listModels(key) }
            .onSuccess { list ->
                if (list.isNotEmpty()) {
                    extraState.update { it.copy(modelOptions = list.map { m -> m.id }) }
                    toasts.emit("已拉取模型 ${list.size} 个")
                } else toasts.emit("模型列表为空")
            }
            .onFailure { t ->
                extraState.update { it.copy(errorMessage = "模型列表错误：${t.message}") }
                toasts.emit("模型拉取失败：${t.message}")
            }
        extraState.update { it.copy(refreshingModels = false) }
    }

    fun refreshBalance() = viewModelScope.launch {
        extraState.update { it.copy(refreshingBalance = true, errorMessage = null) }
        val key = prefs.flow.first().deepseekKey
        runCatching { ai.balance(key) }
            .onSuccess { b ->
                if (b == null) {
                    extraState.update { it.copy(balanceText = "") }
                    toasts.emit("余额暂不可用")
                } else {
                    val info = b.infos.firstOrNull()
                    val txt = info?.let { "${it.currency} ${it.total}" } ?: "已就绪"
                    extraState.update { it.copy(balanceText = txt) }
                    toasts.emit("余额：$txt")
                }
            }
            .onFailure { t ->
                extraState.update { it.copy(errorMessage = "余额错误：${t.message}") }
                toasts.emit("余额刷新失败：${t.message}")
            }
        extraState.update { it.copy(refreshingBalance = false) }
    }
}

private fun ThemeMode.label(): String = when (this) {
    ThemeMode.System -> "跟随系统"
    ThemeMode.Light -> "亮色"
    ThemeMode.Dark -> "暗色"
}

