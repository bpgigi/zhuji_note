package com.zhuji.note.ui.screens.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuji.note.ai.AiAction
import com.zhuji.note.ai.ChatMessage
import com.zhuji.note.ai.ChatRequest
import com.zhuji.note.ai.DeepSeekClient
import com.zhuji.note.data.local.preferences.UserPreferencesDataStore
import com.zhuji.note.domain.model.Note
import com.zhuji.note.domain.usecase.GetNoteUseCase
import com.zhuji.note.domain.usecase.SaveNoteUseCase
import com.zhuji.note.domain.usecase.ToggleFlagsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditUiState(
    val note: Note = Note(title = "", content = ""),
    val loading: Boolean = false,
    val saved: Boolean = false,
    val aiReasoning: String = "",
    val aiAnswer: String = "",
    val aiStreaming: Boolean = false,
    val errorMessage: String? = null,
    val previewMode: Boolean = false,
)

@HiltViewModel
class EditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getNote: GetNoteUseCase,
    private val saveNote: SaveNoteUseCase,
    private val toggle: ToggleFlagsUseCase,
    private val ai: DeepSeekClient,
    private val prefs: UserPreferencesDataStore,
) : ViewModel() {

    private val noteId: Long = savedStateHandle.get<Long>("id") ?: 0L
    private val _state = MutableStateFlow(EditUiState())
    val state: StateFlow<EditUiState> = _state.asStateFlow()
    private var aiJob: Job? = null

    init {
        if (noteId > 0L) {
            viewModelScope.launch {
                runCatching { getNote(noteId).first() }.getOrNull()?.let { existing ->
                    _state.update { it.copy(note = existing) }
                }
            }
        }
    }

    fun onTitle(title: String) {
        if (title == _state.value.note.title) return
        _state.update { it.copy(note = it.note.copy(title = title), saved = false) }
    }
    fun onContent(content: String) {
        if (content == _state.value.note.content) return
        _state.update { it.copy(note = it.note.copy(content = content), saved = false) }
    }
    fun togglePreview() = _state.update { it.copy(previewMode = !it.previewMode) }

    fun togglePinned() = viewModelScope.launch {
        val cur = _state.value.note
        if (cur.id == 0L) return@launch
        toggle(cur.id, ToggleFlagsUseCase.Flag.Pinned, !cur.pinned)
        _state.update { it.copy(note = cur.copy(pinned = !cur.pinned)) }
    }
    fun toggleFavorite() = viewModelScope.launch {
        val cur = _state.value.note
        if (cur.id == 0L) return@launch
        toggle(cur.id, ToggleFlagsUseCase.Flag.Favorite, !cur.favorite)
        _state.update { it.copy(note = cur.copy(favorite = !cur.favorite)) }
    }

    fun save() = viewModelScope.launch {
        _state.update { it.copy(loading = true, errorMessage = null, saved = false) }
        val cur = _state.value.note
        if (cur.title.isBlank() && cur.content.isBlank()) {
            _state.update { it.copy(loading = false) }
            return@launch
        }
        saveNote(cur).onSuccess { id ->
            _state.update { it.copy(loading = false, saved = true, note = it.note.copy(id = id)) }
        }.onFailure { t ->
            _state.update { it.copy(loading = false, errorMessage = t.message ?: "保存失败") }
        }
    }

    fun runAi(action: AiAction, currentText: String, currentTitle: String, extra: String = "") {
        aiJob?.cancel()
        aiJob = viewModelScope.launch {
            _state.update { it.copy(aiReasoning = "", aiAnswer = "", aiStreaming = true, errorMessage = null) }
            val p = prefs.flow.first()
            val key = p.deepseekKey
            val model = p.deepseekModel.ifBlank { "deepseek-v4-flash" }
            if (key.isBlank()) {
                _state.update { it.copy(aiStreaming = false, errorMessage = "请先在设置中填写 DeepSeek API Key") }
                return@launch
            }
            val userText = buildString {
                appendLine("【笔记标题】${currentTitle.ifBlank { "无标题" }}")
                appendLine("【笔记正文】")
                appendLine(currentText.ifBlank { "(空)" })
                if (extra.isNotBlank()) {
                    appendLine()
                    appendLine("【附加请求】")
                    appendLine(extra)
                }
            }
            val req = ChatRequest(
                model = model,
                messages = listOf(
                    ChatMessage("system", action.systemPrompt),
                    ChatMessage("user", userText),
                ),
                stream = true,
                maxTokens = 1024,
                temperature = 0.6,
            )
            try {
                ai.chatStream(key, req).collect { ev ->
                    when (ev) {
                        is DeepSeekClient.StreamEvent.Reasoning ->
                            _state.update { it.copy(aiReasoning = it.aiReasoning + ev.text) }
                        is DeepSeekClient.StreamEvent.Token ->
                            _state.update { it.copy(aiAnswer = it.aiAnswer + ev.text) }
                        is DeepSeekClient.StreamEvent.Finish -> Unit
                        is DeepSeekClient.StreamEvent.Error ->
                            _state.update { it.copy(aiStreaming = false, errorMessage = "AI 出错：${ev.message}") }
                        DeepSeekClient.StreamEvent.Done ->
                            _state.update { it.copy(aiStreaming = false) }
                    }
                }
            } finally {
                _state.update { it.copy(aiStreaming = false) }
            }
        }
    }

    fun cancelAi() { aiJob?.cancel(); _state.update { it.copy(aiStreaming = false) } }

    fun applyAiToContent() = _state.update {
        val combined = if (it.note.content.isBlank()) it.aiAnswer
        else "${it.note.content}\n\n${it.aiAnswer}"
        it.copy(note = it.note.copy(content = combined), aiAnswer = "", aiReasoning = "")
    }
}
