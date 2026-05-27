package com.zhuji.note.stage2

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.zhuji.note.ai.ChatMessage
import com.zhuji.note.ai.ChatRequest
import com.zhuji.note.ai.DeepSeekClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class DeepSeekClientTest {

    private lateinit var server: MockWebServer
    private lateinit var client: DeepSeekClient
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    @Before
    fun setUp() {
        server = MockWebServer().also { it.start() }
        val ok = OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.SECONDS)
            .readTimeout(2, TimeUnit.SECONDS)
            .build()
        client = DeepSeekClient(ok, json, baseUrl = server.url("/").toString().trimEnd('/'))
    }

    @After fun tearDown() { server.shutdown() }

    @Test
    fun `listModels returns parsed entries`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"object":"list","data":[{"id":"deepseek-v4-flash","object":"model","owned_by":"deepseek"}]}"""
            )
        )
        val list = client.listModels("sk-test")
        assertThat(list).hasSize(1)
        assertThat(list.first().id).isEqualTo("deepseek-v4-flash")
    }

    @Test
    fun `chat returns parsed response and sends authorization header`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"id":"x","choices":[{"index":0,"message":{"role":"assistant","content":"hello"}}]}"""
            )
        )
        val req = ChatRequest(model = "deepseek-v4-flash", messages = listOf(ChatMessage("user", "ping")), maxTokens = 4)
        val res = client.chat("sk-test", req)
        assertThat(res.choices.first().message?.content).isEqualTo("hello")
        val recorded = server.takeRequest()
        assertThat(recorded.getHeader("Authorization")).isEqualTo("Bearer sk-test")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `chat without key throws`() = runTest {
        client.chat("", ChatRequest(model = "x", messages = emptyList()))
    }

    @Test
    fun `chatStream emits tokens reasoning and done`() = runTest {
        val streamBody = listOf(
            """data: {"choices":[{"delta":{"reasoning_content":"想"}}]}""",
            "",
            """data: {"choices":[{"delta":{"content":"你"}}]}""",
            "",
            """data: {"choices":[{"delta":{"content":"好"},"finish_reason":"stop"}]}""",
            "",
            "data: [DONE]",
            ""
        ).joinToString("\n")
        server.enqueue(
            MockResponse().setResponseCode(200)
                .addHeader("Content-Type", "text/event-stream")
                .setBody(streamBody)
        )
        val req = ChatRequest(model = "deepseek-v4-flash", messages = listOf(ChatMessage("user", "你好")), stream = true)
        val events = mutableListOf<DeepSeekClient.StreamEvent>()
        client.chatStream("sk-test", req).test(timeout = kotlin.time.Duration.parse("PT5S")) {
            while (true) {
                val item = awaitItem()
                events += item
                if (item is DeepSeekClient.StreamEvent.Done) break
            }
            cancelAndIgnoreRemainingEvents()
        }
        val tokens = events.filterIsInstance<DeepSeekClient.StreamEvent.Token>().joinToString("") { it.text }
        val reasoning = events.filterIsInstance<DeepSeekClient.StreamEvent.Reasoning>().joinToString("") { it.text }
        val finish = events.filterIsInstance<DeepSeekClient.StreamEvent.Finish>()
        assertThat(tokens).isEqualTo("你好")
        assertThat(reasoning).isEqualTo("想")
        assertThat(finish.firstOrNull()?.reason).isEqualTo("stop")
    }

    @Test
    fun `chatStream emits Error on http 401`() = runTest {
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"error":"unauthorized"}"""))
        val req = ChatRequest(model = "x", messages = listOf(ChatMessage("user", "x")), stream = true)
        client.chatStream("sk-bad", req).test {
            val ev = awaitItem()
            assertThat(ev).isInstanceOf(DeepSeekClient.StreamEvent.Error::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
