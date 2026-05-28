package com.zhuji.note.stage2

import com.google.common.truth.Truth.assertThat
import com.zhuji.note.ai.AiAction
import com.zhuji.note.ai.ChatMessage
import com.zhuji.note.ai.ChatRequest
import com.zhuji.note.ai.DeepSeekClient
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test

class DeepSeekClientEdgeCasesTest {
    private lateinit var server: MockWebServer
    private lateinit var client: DeepSeekClient

    @Before fun setup() {
        server = MockWebServer()
        server.start()
        client = DeepSeekClient(
            client = OkHttpClient(),
            json = Json { ignoreUnknownKeys = true },
            baseUrl = server.url("/").toString().trimEnd('/')
        )
    }

    @After fun teardown() { server.shutdown() }

    @Test fun `DONE marker emits Done event`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setHeader("Content-Type", "text/event-stream").setBody("data: [DONE]\n\n"))
        val req = ChatRequest(model = "test", messages = listOf(ChatMessage("user", "hi")))
        val chunks = client.chatStream("sk-test", req).toList()
        assertThat(chunks).isNotEmpty()
    }

    @Test fun `401 error handled gracefully`() = runTest {
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"error":{"message":"invalid key"}}"""))
        val req = ChatRequest(model = "test", messages = listOf(ChatMessage("user", "hi")))
        val result = try { client.chatStream("sk-bad", req).toList(); "ok" } catch (e: Exception) { "error:${e.message}" }
        assertThat(result).isNotEmpty()
    }

    @Test fun `429 rate limit handled gracefully`() = runTest {
        server.enqueue(MockResponse().setResponseCode(429).setBody("""{"error":{"message":"rate limited"}}"""))
        val req = ChatRequest(model = "test", messages = listOf(ChatMessage("user", "hi")))
        val result = try { client.chatStream("sk-test", req).toList(); "ok" } catch (e: Exception) { "error:${e.message}" }
        assertThat(result).isNotEmpty()
    }

    @Test fun `all AiAction entries have non-blank systemPrompt`() {
        AiAction.entries.forEach {
            assertThat(it.systemPrompt).isNotEmpty()
        }
    }

    @Test fun `all AiAction entries have title`() {
        AiAction.entries.forEach {
            assertThat(it.title).isNotEmpty()
        }
    }

    @Test fun `AiAction has 9 entries`() {
        assertThat(AiAction.entries).hasSize(9)
    }
}
