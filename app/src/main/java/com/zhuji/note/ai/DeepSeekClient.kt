package com.zhuji.note.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class DeepSeekClient(
    private val client: OkHttpClient,
    private val json: Json,
    private val baseUrl: String = "https://api.deepseek.com",
) {

    private fun apiKeyHeader(key: String) = "Bearer ${key.trim()}"

    suspend fun listModels(apiKey: String): List<ModelEntry> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext DEFAULT_MODELS
        val req = Request.Builder()
            .url("$baseUrl/models")
            .header("Authorization", apiKeyHeader(apiKey))
            .get()
            .build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) error("models http=${resp.code}")
            val body = resp.body?.string().orEmpty()
            json.decodeFromString(ModelList.serializer(), body).data
        }
    }

    suspend fun balance(apiKey: String): BalanceResponse? = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext null
        val req = Request.Builder()
            .url("$baseUrl/user/balance")
            .header("Authorization", apiKeyHeader(apiKey))
            .get()
            .build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return@withContext null
            json.decodeFromString(BalanceResponse.serializer(), resp.body?.string().orEmpty())
        }
    }

    suspend fun chat(apiKey: String, request: ChatRequest): ChatResponse = withContext(Dispatchers.IO) {
        require(apiKey.isNotBlank()) { "缺少 DeepSeek API Key，请在设置中填写。" }
        val payload = json.encodeToString(ChatRequest.serializer(), request.copy(stream = false))
        val req = Request.Builder()
            .url("$baseUrl/chat/completions")
            .header("Authorization", apiKeyHeader(apiKey))
            .post(payload.toRequestBody(JSON))
            .build()
        client.newCall(req).execute().use { resp ->
            val body = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) error("AI 调用失败 http=${resp.code} body=${body.take(180)}")
            json.decodeFromString(ChatResponse.serializer(), body)
        }
    }

    fun chatStream(apiKey: String, request: ChatRequest): Flow<StreamEvent> = flow<StreamEvent> {
        require(apiKey.isNotBlank()) { "缺少 DeepSeek API Key，请在设置中填写。" }
        val payload = json.encodeToString(ChatRequest.serializer(), request.copy(stream = true))
        val req = Request.Builder()
            .url("$baseUrl/chat/completions")
            .header("Authorization", apiKeyHeader(apiKey))
            .header("Accept", "text/event-stream")
            .header("Accept-Encoding", "identity")
            .post(payload.toRequestBody(JSON))
            .build()
        val call = client.newCall(req)
        try {
            call.execute().use { resp ->
                if (!resp.isSuccessful) error("流式调用失败 http=${resp.code} body=${resp.body?.string().orEmpty().take(180)}")
                val source = resp.body?.source() ?: error("空响应体")
                while (!source.exhausted()) {
                    val line = source.readUtf8Line() ?: break
                    if (line.isBlank()) continue
                    if (line.startsWith(":")) continue
                    if (!line.startsWith("data:")) continue
                    val data = line.substring(5).trim()
                    if (data == "[DONE]") {
                        emit(StreamEvent.Done)
                        return@use
                    }
                    runCatching {
                        json.decodeFromString(ChatResponse.serializer(), data)
                    }.onSuccess { chunk ->
                        val choice = chunk.choices.firstOrNull() ?: return@onSuccess
                        choice.delta?.reasoningContent?.takeIf { it.isNotEmpty() }?.let {
                            emit(StreamEvent.Reasoning(it))
                        }
                        choice.delta?.content?.takeIf { it.isNotEmpty() }?.let {
                            emit(StreamEvent.Token(it))
                        }
                        choice.finishReason?.let { emit(StreamEvent.Finish(it)) }
                    }
                }
            }
        } catch (t: Throwable) {
            emit(StreamEvent.Error(t.message ?: t::class.java.simpleName))
        }
    }.flowOn(Dispatchers.IO)

    sealed interface StreamEvent {
        data class Token(val text: String) : StreamEvent
        data class Reasoning(val text: String) : StreamEvent
        data class Finish(val reason: String) : StreamEvent
        data class Error(val message: String) : StreamEvent
        data object Done : StreamEvent
    }

    companion object {
        private val JSON = "application/json; charset=utf-8".toMediaType()
        val DEFAULT_MODELS = listOf(
            ModelEntry("deepseek-v4-flash", "model", "deepseek"),
            ModelEntry("deepseek-v4-pro", "model", "deepseek"),
        )
    }
}

