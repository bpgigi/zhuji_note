package com.zhuji.note.ui.screens.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhuji.note.ai.AiAction
import com.zhuji.note.ui.common.MarkdownView
import com.zhuji.note.ui.common.TypingDot
import com.zhuji.note.ui.theme.Spacing
import com.zhuji.note.ui.theme.ZhujiCornerTokens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(noteId: Long, onBack: () -> Unit, vm: EditViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snackHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var aiOpen by remember { mutableStateOf(false) }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { msg ->
            scope.launch { snackHost.showSnackbar(msg) }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackHost) },
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == 0L) "新笔记" else "编辑", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.note.title.isNotBlank() || state.note.content.isNotBlank()) vm.save()
                        onBack()
                    }) { Icon(Icons.Outlined.ArrowBack, contentDescription = "返回") }
                },
                actions = {
                    IconButton(onClick = vm::togglePinned) { Icon(Icons.Outlined.PushPin, null, tint = if (state.note.pinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) }
                    IconButton(onClick = vm::toggleFavorite) { Icon(if (state.note.favorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder, null, tint = if (state.note.favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) }
                    IconButton(onClick = vm::togglePreview) { Icon(if (state.previewMode) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, null) }
                    IconButton(onClick = vm::save) { Icon(Icons.Outlined.Done, contentDescription = "保存", tint = if (state.saved) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { aiOpen = true },
                shape = ZhujiCornerTokens.FAB,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) { Icon(Icons.Outlined.AutoAwesome, contentDescription = "AI 助手") }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = Spacing.lg)
        ) {
            Spacer(Modifier.height(Spacing.sm))
            OutlinedTextField(
                value = state.note.title,
                onValueChange = vm::onTitle,
                placeholder = { Text("标题") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
            )
            Spacer(Modifier.height(Spacing.sm))
            if (state.previewMode) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    MarkdownView(state.note.content.ifBlank { "_(还没有内容)_" })
                }
            } else {
                OutlinedTextField(
                    value = state.note.content,
                    onValueChange = vm::onContent,
                    placeholder = { Text("写点什么吧… 支持 Markdown") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Default),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )
            }
            Surface(
                tonalElevation = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = ZhujiCornerTokens.NoteCard,
                modifier = Modifier.padding(vertical = Spacing.sm).fillMaxWidth()
            ) {
                Row(Modifier.padding(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Bolt, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.padding(start = Spacing.xs))
                    Text("${state.note.wordCount} 字 · ${if (state.saved) "已保存" else "未保存"}", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        if (aiOpen) {
            AiSheet(
                state = state,
                onClose = { aiOpen = false },
                onAction = { action -> vm.runAi(action) },
                onCancel = vm::cancelAi,
                onApply = { vm.applyAiToContent(); aiOpen = false },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AiSheet(
    state: EditUiState,
    onClose: () -> Unit,
    onAction: (AiAction) -> Unit,
    onCancel: () -> Unit,
    onApply: () -> Unit,
) {
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onClose,
        shape = ZhujiCornerTokens.Sheet,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(Modifier.padding(horizontal = Spacing.xl, vertical = Spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.padding(start = Spacing.xs))
                Text("助记 AI · DeepSeek", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onClose) { Icon(Icons.Outlined.Close, null) }
            }
            Spacer(Modifier.height(Spacing.sm))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                items(AiAction.values().toList()) { action ->
                    AssistChip(
                        onClick = { onAction(action) },
                        label = { Text(action.title) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                        shape = ZhujiCornerTokens.Chip,
                    )
                }
            }
            Spacer(Modifier.height(Spacing.lg))
            if (state.aiReasoning.isNotBlank()) {
                Surface(
                    tonalElevation = 1.dp,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = ZhujiCornerTokens.NoteCard,
                ) {
                    Column(Modifier.padding(Spacing.md)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("思考中…", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onTertiaryContainer)
                            Spacer(Modifier.padding(start = Spacing.xs))
                            TypingDot()
                        }
                        Spacer(Modifier.height(Spacing.xs))
                        Text(state.aiReasoning, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                }
                Spacer(Modifier.height(Spacing.md))
            }
            if (state.aiAnswer.isNotBlank()) {
                MarkdownView(state.aiAnswer)
                Spacer(Modifier.height(Spacing.lg))
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    AssistChip(onClick = onApply, label = { Text("应用到笔记") }, leadingIcon = { Icon(Icons.Outlined.Done, null) })
                    AssistChip(onClick = onCancel, label = { Text("停止") }, leadingIcon = { Icon(Icons.Outlined.CleaningServices, null) })
                }
            } else if (state.aiStreaming) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TypingDot()
                    Spacer(Modifier.padding(start = Spacing.xs))
                    Text("AI 正在生成…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Text("点击上方任一按钮，让 AI 帮你处理这条笔记。", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(Spacing.xxl))
        }
    }
}
