package com.zhuji.note.ui.screens.edit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextRange
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhuji.note.ai.AiAction
import com.zhuji.note.domain.util.MarkdownToolbar
import com.zhuji.note.ui.common.Clipboard
import com.zhuji.note.ui.common.GradientProgressBar
import com.zhuji.note.ui.common.MarkdownView
import com.zhuji.note.ui.common.TopLoadingLine
import com.zhuji.note.ui.common.TypingDot
import com.zhuji.note.ui.theme.Spacing
import com.zhuji.note.ui.theme.ZhujiCornerTokens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(noteId: Long, onBack: () -> Unit, onOpenSettings: () -> Unit = {}, vm: EditViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snackHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var aiOpen by remember { mutableStateOf(false) }

    var titleField by remember(state.note.id) { mutableStateOf(TextFieldValue(state.note.title)) }
    var bodyField by remember(state.note.id) { mutableStateOf(TextFieldValue(state.note.content)) }

    LaunchedEffect(state.note.title, state.note.content) {
        if (titleField.text != state.note.title) titleField = TextFieldValue(state.note.title, TextRange(state.note.title.length))
        if (bodyField.text != state.note.content) bodyField = TextFieldValue(state.note.content, TextRange(state.note.content.length))
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { msg ->
            scope.launch { snackHost.showSnackbar(msg) }
        }
    }

    LaunchedEffect(state.saved) {
        if (state.saved) scope.launch { snackHost.showSnackbar("已保存 ✓") }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackHost) },
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(if (noteId == 0L) "新笔记" else "编辑", style = MaterialTheme.typography.titleMedium) },
                    navigationIcon = {
                        IconButton(onClick = {
                            vm.onTitle(titleField.text)
                            vm.onContent(bodyField.text)
                            if (titleField.text.isNotBlank() || bodyField.text.isNotBlank()) vm.save()
                            onBack()
                        }) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回") }
                    },
                    actions = {
                        IconButton(onClick = vm::togglePinned) {
                            Icon(Icons.Outlined.PushPin, null, tint = if (state.note.pinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                        }
                        IconButton(onClick = vm::toggleFavorite) {
                            Icon(if (state.note.favorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder, null, tint = if (state.note.favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                        }
                        IconButton(onClick = vm::togglePreview) {
                            Icon(if (state.previewMode) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, null)
                        }
                        IconButton(onClick = {
                            vm.onTitle(titleField.text); vm.onContent(bodyField.text); vm.save()
                        }) {
                            Icon(Icons.Outlined.Done, contentDescription = "保存", tint = if (state.saved) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                )
                TopLoadingLine(loading = state.loading)
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { aiOpen = true },
                shape = ZhujiCornerTokens.FAB,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.semantics { contentDescription = "打开 AI 助手" }
            ) { Icon(Icons.Outlined.AutoAwesome, contentDescription = null) }
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
                value = titleField,
                onValueChange = { titleField = it; vm.onTitle(it.text) },
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
            Spacer(Modifier.height(Spacing.xs))
            if (state.previewMode) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    MarkdownView(bodyField.text.ifBlank { "_(还没有内容)_" })
                }
            } else {
                Box(Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = bodyField,
                        onValueChange = { bodyField = it; vm.onContent(it.text) },
                        placeholder = { Text("写点什么吧… 支持 Markdown") },
                        modifier = Modifier.fillMaxSize(),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Default),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        )
                    )
                }
            }
            // Markdown 工具栏
            if (!state.previewMode) MarkdownToolbarRow(
                onAction = { kind ->
                    val sel = bodyField.selection
                    val a = sel.start; val b = sel.end
                    val (newText, range) = when (kind) {
                        "h1" -> MarkdownToolbar.applyHeading(bodyField.text, a, b, 1)
                        "h2" -> MarkdownToolbar.applyHeading(bodyField.text, a, b, 2)
                        "bold" -> MarkdownToolbar.applyBold(bodyField.text, a, b)
                        "italic" -> MarkdownToolbar.applyItalic(bodyField.text, a, b)
                        "code" -> MarkdownToolbar.applyInlineCode(bodyField.text, a, b)
                        "ul" -> MarkdownToolbar.applyBulletList(bodyField.text, a, b)
                        "ol" -> MarkdownToolbar.applyNumberedList(bodyField.text, a, b)
                        "check" -> MarkdownToolbar.applyChecklist(bodyField.text, a, b)
                        "quote" -> MarkdownToolbar.applyQuote(bodyField.text, a, b)
                        "link" -> MarkdownToolbar.applyLink(bodyField.text, a, b)
                        else -> bodyField.text to (a..b)
                    }
                    bodyField = TextFieldValue(newText, TextRange(range.last))
                    vm.onContent(newText)
                }
            )
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
                    Spacer(Modifier.weight(1f))
                    AssistChip(
                        onClick = {
                            Clipboard.copy(context, "笔记", bodyField.text)
                            scope.launch { snackHost.showSnackbar("已复制到剪贴板") }
                        },
                        label = { Text("复制全文") },
                        leadingIcon = { Icon(Icons.Outlined.ContentCopy, null, modifier = Modifier.size(18.dp)) },
                    )
                }
            }
        }

        if (aiOpen) {
            AiSheet(
                state = state,
                onClose = { aiOpen = false },
                onAction = { action -> vm.runAi(action, currentText = bodyField.text, currentTitle = titleField.text) },
                onCancel = vm::cancelAi,
                onOpenSettings = {
                    aiOpen = false
                    onOpenSettings()
                },
                onApply = {
                    val merged = if (bodyField.text.isBlank()) state.aiAnswer else "${bodyField.text}\n\n${state.aiAnswer}"
                    bodyField = TextFieldValue(merged, TextRange(merged.length))
                    vm.onContent(merged)
                    vm.applyAiToContent()
                    scope.launch { snackHost.showSnackbar("AI 答案已合并到笔记") }
                    aiOpen = false
                },
                onCopy = {
                    if (state.aiAnswer.isNotBlank()) {
                        Clipboard.copy(context, "AI 输出", state.aiAnswer)
                        scope.launch { snackHost.showSnackbar("已复制 AI 输出") }
                    }
                },
            )
        }
    }
}

@Composable
private fun MarkdownToolbarRow(onAction: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xxs),
    ) {
        ToolItem("H1", Icons.Outlined.Title, onClick = { onAction("h1") })
        ToolItem("H2", Icons.Outlined.Title, onClick = { onAction("h2") })
        ToolItem("加粗", Icons.Outlined.FormatBold, onClick = { onAction("bold") })
        ToolItem("斜体", Icons.Outlined.FormatItalic, onClick = { onAction("italic") })
        ToolItem("代码", Icons.Outlined.Code, onClick = { onAction("code") })
        ToolItem("列表", Icons.Outlined.FormatListBulleted, onClick = { onAction("ul") })
        ToolItem("有序", Icons.Outlined.FormatListNumbered, onClick = { onAction("ol") })
        ToolItem("待办", Icons.Outlined.Checklist, onClick = { onAction("check") })
        ToolItem("引用", Icons.Outlined.FormatQuote, onClick = { onAction("quote") })
        ToolItem("链接", Icons.Outlined.Link, onClick = { onAction("link") })
    }
}

@Composable
private fun ToolItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        leadingIcon = { Icon(icon, null, modifier = Modifier.size(16.dp)) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        shape = ZhujiCornerTokens.Chip,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AiSheet(
    state: EditUiState,
    onClose: () -> Unit,
    onAction: (AiAction) -> Unit,
    onCancel: () -> Unit,
    onOpenSettings: () -> Unit,
    onApply: () -> Unit,
    onCopy: () -> Unit,
) {
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        shape = ZhujiCornerTokens.Sheet,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xl)
                .padding(bottom = Spacing.lg)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.padding(start = Spacing.xs))
                Text("助记 AI · DeepSeek", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onClose) { Icon(Icons.Outlined.Close, null) }
            }
            Spacer(Modifier.height(Spacing.xs))
            GradientProgressBar(visible = state.aiStreaming)
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
            Column(
                Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
            ) {
                if (state.aiReasoning.isNotBlank()) {
                    ReasoningCard(
                        reasoning = state.aiReasoning,
                        streaming = state.aiStreaming,
                        answerReady = state.aiAnswer.isNotBlank(),
                    )
                    Spacer(Modifier.height(Spacing.md))
                }
                AnimatedVisibility(state.aiAnswer.isNotBlank(), enter = fadeIn() + slideInVertically(), exit = fadeOut()) {
                    MarkdownView(state.aiAnswer)
                }
                if (state.aiAnswer.isBlank() && state.aiStreaming && state.aiReasoning.isBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TypingDot()
                        Spacer(Modifier.padding(start = Spacing.xs))
                        Text("AI 正在生成…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else if (state.errorMessage != null && state.aiAnswer.isBlank() && state.aiReasoning.isBlank()) {
                    KeyErrorCard(message = state.errorMessage, onOpenSettings = onOpenSettings)
                } else if (state.aiAnswer.isBlank() && state.aiReasoning.isBlank() && !state.aiStreaming) {
                    Text("点击上方任一按钮，让 AI 帮你处理这条笔记。", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (state.errorMessage != null && !state.aiStreaming && (state.aiReasoning.isNotBlank() || state.aiAnswer.isNotBlank())) {
                    Spacer(Modifier.height(Spacing.md))
                    KeyErrorCard(message = state.errorMessage, onOpenSettings = onOpenSettings)
                }
            }
            if (state.aiAnswer.isNotBlank() || state.aiStreaming) {
                Spacer(Modifier.height(Spacing.md))
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    if (state.aiAnswer.isNotBlank()) {
                        AssistChip(onClick = onApply, label = { Text("应用到笔记") }, leadingIcon = { Icon(Icons.Outlined.Done, null) })
                        AssistChip(onClick = onCopy, label = { Text("复制") }, leadingIcon = { Icon(Icons.Outlined.ContentCopy, null) })
                    }
                    if (state.aiStreaming) {
                        AssistChip(onClick = onCancel, label = { Text("停止") }, leadingIcon = { Icon(Icons.Outlined.Close, null) })
                    }
                }
            }
            Spacer(Modifier.height(Spacing.md))
        }
    }
}

@Composable
private fun KeyErrorCard(message: String, onOpenSettings: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = ZhujiCornerTokens.NoteCard,
    ) {
        Column(Modifier.padding(Spacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.ErrorOutline, null, tint = MaterialTheme.colorScheme.onErrorContainer)
                Spacer(Modifier.padding(start = Spacing.xs))
                Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
            }
            if (message.contains("API Key")) {
                Spacer(Modifier.height(Spacing.sm))
                AssistChip(
                    onClick = onOpenSettings,
                    label = { Text("去设置填写 Key") },
                    leadingIcon = { Icon(Icons.Outlined.Settings, null) },
                )
            }
        }
    }
}

@Composable
private fun ReasoningCard(reasoning: String, streaming: Boolean, answerReady: Boolean) {
    var expanded by remember { mutableStateOf(true) }
    LaunchedEffect(answerReady) {
        if (answerReady) expanded = false
    }
    Surface(
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = ZhujiCornerTokens.NoteCard,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Psychology, null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                Spacer(Modifier.padding(start = Spacing.xs))
                Text(
                    if (streaming && !answerReady) "深度思考中…" else "已深度思考",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                if (streaming && !answerReady) {
                    Spacer(Modifier.padding(start = Spacing.xs))
                    TypingDot()
                }
                Spacer(Modifier.weight(1f))
                Icon(
                    if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = if (expanded) "收起思考" else "展开思考",
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
            if (expanded) {
                Text(
                    reasoning,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(start = Spacing.md, end = Spacing.md, bottom = Spacing.md),
                )
            }
        }
    }
}



