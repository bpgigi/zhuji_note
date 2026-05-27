package com.zhuji.note.stage1

import com.google.common.truth.Truth.assertThat
import com.zhuji.note.ai.AiAction
import com.zhuji.note.ai.ChatMessage
import com.zhuji.note.ai.ChatRequest
import com.zhuji.note.ai.ChatResponse
import com.zhuji.note.ai.ChatChoice
import com.zhuji.note.ai.ChatDelta
import kotlinx.serialization.json.Json
import org.junit.Test

class AiSerializationTest {
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    @Test fun `every AiAction has chinese title`() {
        AiAction.values().forEach { a ->
            assertThat(a.title).isNotEmpty()
            assertThat(a.systemPrompt).isNotEmpty()
        }
    }

    @Test fun `chat request serializes essential fields`() {
        val req = ChatRequest(model = "deepseek-v4-flash", messages = listOf(ChatMessage("user", "hi")), stream = false, maxTokens = 8)
        val text = json.encodeToString(ChatRequest.serializer(), req)
        assertThat(text).contains("\"model\":\"deepseek-v4-flash\"")
        assertThat(text).contains("\"messages\":[")
        assertThat(text).contains("\"max_tokens\":8")
    }

    @Test fun `chat response parses choices`() {
        val raw = """{"id":"x","choices":[{"index":0,"message":{"role":"assistant","content":"hi"}}],"model":"deepseek-v4-flash"}"""
        val res = json.decodeFromString(ChatResponse.serializer(), raw)
        assertThat(res.choices).hasSize(1)
        assertThat(res.choices.first().message?.content).isEqualTo("hi")
    }

    @Test fun `delta with reasoning content`() {
        val raw = """{"choices":[{"delta":{"role":"assistant","reasoning_content":"思考中…"}}]}"""
        val res = json.decodeFromString(ChatResponse.serializer(), raw)
        assertThat(res.choices.first().delta?.reasoningContent).isEqualTo("思考中…")
    }

    @Test fun `unknown fields are ignored`() {
        val raw = """{"choices":[],"foo":1,"bar":"x"}"""
        val res = json.decodeFromString(ChatResponse.serializer(), raw)
        assertThat(res.choices).isEmpty()
    }
}
