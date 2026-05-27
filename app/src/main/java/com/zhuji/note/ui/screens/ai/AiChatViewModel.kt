package com.zhuji.note.ui.screens.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuji.note.ai.ChatMessage
import com.zhuji.note.ai.ChatRequest
import com.zhuji.note.ai.DeepSeekClient
import com.zhuji.note.data.local.preferences.UserPreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatBubble(val role: String, val content: String)

data class AiChatUiState(
    val messages: List<ChatBubble> = listOf(
        ChatBubble("assistant", "嗨，我是助记 AI（DeepSeek 驱动），可以帮你提问笔记内容、做摘要、续写等。"),
    ),
    val streaming: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val ai: DeepSeekClient,
    private val prefs: UserPreferencesDataStore,
) : ViewModel() {
    private val _state = MutableStateFlow(AiChatUiState())
    val state: StateFlow<AiChatUiState> = _state.asStateFlow()
    private var job: Job? = null

    fun send(text: String) {
        job?.cancel()
        _state.update { it.copy(messages = it.messages + ChatBubble("user", text), streaming = true, errorMessage = null) }
        job = viewModelScope.launch {
            val p = prefs.flow.first()
            if (p.deepseekKey.isBlank()) {
                _state.update { it.copy(streaming = false, errorMessage = "请先在设置中填写 DeepSeek API Key。") }
                return@launch
            }
            val history = _state.value.messages.takeLast(10).map { ChatMessage(it.role, it.content) }
            val req = ChatRequest(
                model = p.deepseekModel,
                messages = listOf(ChatMessage("system", "你是一个简洁、有同理心的中文 AI 笔记助手。给出干净的 Markdown 答案。")) + history,
                stream = true,
                maxTokens = 1024,
                temperature = 0.7,
            )
            // 占位 assistant 气泡，逐字追加
            _state.update { it.copy(messages = it.messages + ChatBubble("assistant", "")) }
            ai.chatStream(p.deepseekKey, req).collect { ev ->
                when (ev) {
                    is DeepSeekClient.StreamEvent.Token -> _state.update { s ->
                        val list = s.messages.toMutableList()
                        val last = list.last()
                        list[list.lastIndex] = last.copy(content = last.content + ev.text)
                        s.copy(messages = list)
                    }
                    is DeepSeekClient.StreamEvent.Reasoning -> Unit
                    is DeepSeekClient.StreamEvent.Finish -> Unit
                    is DeepSeekClient.StreamEvent.Error -> _state.update { it.copy(streaming = false, errorMessage = ev.message) }
                    DeepSeekClient.StreamEvent.Done -> _state.update { it.copy(streaming = false) }
                }
            }
            _state.update { it.copy(streaming = false) }
        }
    }

    fun cancel() { job?.cancel(); _state.update { it.copy(streaming = false) } }
}
