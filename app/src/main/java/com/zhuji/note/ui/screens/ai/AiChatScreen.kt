package com.zhuji.note.ui.screens.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhuji.note.ui.common.MarkdownView
import com.zhuji.note.ui.common.TypingDot
import com.zhuji.note.ui.theme.Spacing
import com.zhuji.note.ui.theme.ZhujiCornerTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(onBack: () -> Unit, vm: AiChatViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    var input by remember { mutableStateOf("") }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI 助手") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "返回") } },
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(Spacing.lg)
        ) {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                items(state.messages) { msg ->
                    val bubbleColor = if (msg.role == "user") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    val onBubble = if (msg.role == "user") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    val align = if (msg.role == "user") Alignment.End else Alignment.Start
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = align) {
                        Surface(
                            shape = ZhujiCornerTokens.NoteCard,
                            color = bubbleColor,
                            contentColor = onBubble,
                        ) {
                            Box(Modifier.padding(Spacing.md)) {
                                if (msg.role == "assistant") MarkdownView(msg.content)
                                else Text(msg.content, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
                if (state.streaming) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.padding(start = Spacing.xs))
                            TypingDot()
                            Spacer(Modifier.padding(start = Spacing.xs))
                            Text("AI 思考中…", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
            state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(Spacing.xs))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("用自然语言提问；DeepSeek 流式响应") },
                    keyboardOptions = KeyboardOptions.Default,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )
                Spacer(Modifier.padding(start = Spacing.sm))
                if (state.streaming) {
                    IconButton(onClick = vm::cancel) { Icon(Icons.Outlined.Stop, null, tint = MaterialTheme.colorScheme.error) }
                } else {
                    IconButton(onClick = {
                        if (input.isNotBlank()) {
                            vm.send(input.trim())
                            input = ""
                        }
                    }) { Icon(Icons.Outlined.Send, null, tint = MaterialTheme.colorScheme.primary) }
                }
            }
        }
    }
}
