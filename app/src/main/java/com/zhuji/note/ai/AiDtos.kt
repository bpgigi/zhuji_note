package com.zhuji.note.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val role: String,
    val content: String,
)

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val stream: Boolean = false,
    @SerialName("max_tokens") val maxTokens: Int? = null,
    val temperature: Double? = null,
)

@Serializable
data class ChatChoice(
    val index: Int = 0,
    val message: ChatMessage? = null,
    val delta: ChatDelta? = null,
    @SerialName("finish_reason") val finishReason: String? = null,
)

@Serializable
data class ChatDelta(
    val role: String? = null,
    val content: String? = null,
    @SerialName("reasoning_content") val reasoningContent: String? = null,
)

@Serializable
data class Usage(
    @SerialName("prompt_tokens") val promptTokens: Int = 0,
    @SerialName("completion_tokens") val completionTokens: Int = 0,
    @SerialName("total_tokens") val totalTokens: Int = 0,
)

@Serializable
data class ChatResponse(
    val id: String? = null,
    val choices: List<ChatChoice> = emptyList(),
    val usage: Usage? = null,
    val model: String? = null,
)

@Serializable
data class ModelEntry(
    val id: String,
    val `object`: String? = null,
    @SerialName("owned_by") val ownedBy: String? = null,
)

@Serializable
data class ModelList(
    val `object`: String? = null,
    val data: List<ModelEntry> = emptyList(),
)

@Serializable
data class BalanceInfo(
    val currency: String,
    @SerialName("total_balance") val total: String,
    @SerialName("granted_balance") val granted: String? = null,
    @SerialName("topped_up_balance") val toppedUp: String? = null,
)

@Serializable
data class BalanceResponse(
    @SerialName("is_available") val available: Boolean,
    @SerialName("balance_infos") val infos: List<BalanceInfo> = emptyList(),
)
