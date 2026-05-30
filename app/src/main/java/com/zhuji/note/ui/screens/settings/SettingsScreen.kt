package com.zhuji.note.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SettingsBrightness
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhuji.note.data.local.preferences.ThemeMode
import com.zhuji.note.ui.common.GradientProgressBar
import com.zhuji.note.ui.common.TopLoadingLine
import com.zhuji.note.ui.theme.Spacing
import com.zhuji.note.ui.theme.ZhujiCornerTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, vm: SettingsViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }
    var keyDraft by remember(state.savedKey) { mutableStateOf(state.savedKey) }
    var revealKey by remember { mutableStateOf(false) }

    val exportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/zip")
    ) { uri -> uri?.let { vm.exportZip(it) } }
    val importLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { vm.importMarkdown(it) } }

    LaunchedEffect(Unit) {
        vm.toastsFlow.collect { msg -> snack.showSnackbar(msg) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("设置", style = MaterialTheme.typography.titleMedium) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                )
                TopLoadingLine(loading = state.verifying || state.refreshingBalance || state.refreshingModels)
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.xl)
        ) {
            Spacer(Modifier.height(Spacing.md))
            SectionTitle("外观")
            ThemeSelector(current = state.theme, onPick = vm::setTheme)
            Spacer(Modifier.height(Spacing.sm))
            ToggleRow(
                title = "动态取色",
                description = "Android 12+ 用壁纸主色生成主题",
                checked = state.dynamicColor,
                onCheckedChange = vm::setDynamicColor,
            )
            ToggleRow(
                title = "霓虹强调（暗色）",
                description = "夜晚使用 Windsurf 霓虹青替代暖橙",
                checked = state.accentNeon,
                onCheckedChange = vm::setNeon,
            )
            Spacer(Modifier.height(Spacing.lg))

            SectionTitle("AI 助手 · DeepSeek")
            Card {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.padding(start = Spacing.xs))
                    Text("API Key", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.weight(1f))
                    if (state.balanceText.isNotBlank()) {
                        AssistChip(
                            onClick = vm::refreshBalance,
                            label = { Text(state.balanceText) },
                            leadingIcon = { Icon(Icons.Outlined.AccountBalanceWallet, null, modifier = Modifier.size(16.dp)) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            ),
                        )
                    }
                }
                Spacer(Modifier.height(Spacing.xs))
                OutlinedTextField(
                    value = keyDraft,
                    onValueChange = { keyDraft = it; vm.saveKeyDraft(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("sk-...") },
                    singleLine = true,
                    visualTransformation = if (revealKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(onClick = { revealKey = !revealKey }) {
                            Text(if (revealKey) "隐藏" else "显示")
                        }
                    },
                    keyboardOptions = KeyboardOptions.Default,
                )
                Spacer(Modifier.height(Spacing.xs))
                GradientProgressBar(visible = state.verifying)
                Spacer(Modifier.height(Spacing.xs))
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { vm.saveAndVerifyKey(keyDraft) },
                        enabled = !state.verifying,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    ) {
                        if (state.verifying) {
                            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                            Spacer(Modifier.padding(start = Spacing.xs))
                            Text("验证中…")
                        } else {
                            Icon(Icons.Outlined.VerifiedUser, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.padding(start = Spacing.xs))
                            Text("保存并验证")
                        }
                    }
                    AssistChip(
                        onClick = vm::refreshBalance,
                        enabled = !state.refreshingBalance,
                        label = { Text(if (state.refreshingBalance) "刷新中…" else "查询余额") },
                        leadingIcon = { Icon(Icons.Outlined.Refresh, null, modifier = Modifier.size(16.dp)) },
                    )
                }
                state.errorMessage?.let {
                    Spacer(Modifier.height(Spacing.xs))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(Spacing.sm))
            Card {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("模型", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.weight(1f))
                    AssistChip(
                        onClick = vm::refreshModels,
                        enabled = !state.refreshingModels,
                        label = { Text(if (state.refreshingModels) "拉取中…" else "刷新模型") },
                        leadingIcon = { Icon(Icons.Outlined.Refresh, null, modifier = Modifier.size(16.dp)) },
                    )
                }
                Spacer(Modifier.height(Spacing.xs))
                ModelSelector(state.modelOptions, state.model, vm::setModel)
            }

            Spacer(Modifier.height(Spacing.lg))
            SectionTitle("数据备份")
            Card {
                Column(Modifier.padding(Spacing.md)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Backup, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.padding(start = Spacing.sm))
                        Column(Modifier.weight(1f)) {
                            Text("导出与导入", style = MaterialTheme.typography.titleMedium)
                            Text("导出全部笔记为 ZIP，或从 Markdown 文件导入", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.height(Spacing.sm))
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        AssistChip(
                            onClick = { exportLauncher.launch("zhuji_backup.zip") },
                            label = { Text("导出 ZIP") },
                            leadingIcon = { Icon(Icons.Outlined.Upload, null) },
                        )
                        AssistChip(
                            onClick = { importLauncher.launch(arrayOf("text/markdown", "text/plain", "*/*")) },
                            label = { Text("导入 Markdown") },
                            leadingIcon = { Icon(Icons.Outlined.Download, null) },
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.lg))
            SectionTitle("关于")
            Card {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Info, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.padding(start = Spacing.sm))
                    Column(Modifier.weight(1f)) {
                        Text("助记 ZhujiNote", style = MaterialTheme.typography.titleMedium)
                        Text("MT2026 移动应用测试 · 大作业", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.height(Spacing.huge))
        }
    }
}

@Composable
private fun SectionTitle(label: String) {
    Text(
        label.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = Spacing.xs)
    )
}

@Composable
private fun Card(content: @Composable () -> Unit) {
    Surface(
        shape = ZhujiCornerTokens.NoteCard,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs)
    ) {
        Column(Modifier.padding(Spacing.lg)) { content() }
    }
}

@Composable
private fun ToggleRow(title: String, description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun ThemeSelector(current: ThemeMode, onPick: (ThemeMode) -> Unit) {
    Card {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("主题", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(1f))
            ThemePill(label = "亮", icon = Icons.Outlined.LightMode, selected = current == ThemeMode.Light) { onPick(ThemeMode.Light) }
            Spacer(Modifier.padding(start = Spacing.xs))
            ThemePill(label = "暗", icon = Icons.Outlined.DarkMode, selected = current == ThemeMode.Dark) { onPick(ThemeMode.Dark) }
            Spacer(Modifier.padding(start = Spacing.xs))
            ThemePill(label = "随系统", icon = Icons.Outlined.SettingsBrightness, selected = current == ThemeMode.System) { onPick(ThemeMode.System) }
        }
    }
}

@Composable
private fun ThemePill(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = ZhujiCornerTokens.Chip,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xxs)) {
            Icon(icon, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.padding(start = Spacing.xxs))
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelSelector(options: List<String>, current: String, onPick: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { open = true },
        shape = ZhujiCornerTokens.Input,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(Modifier.padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Palette, null)
            Spacer(Modifier.padding(start = Spacing.sm))
            Text(current.ifBlank { "选择模型" }, style = MaterialTheme.typography.bodyLarge)
        }
    }
    DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
        options.forEach { m ->
            DropdownMenuItem(text = { Text(m) }, onClick = { onPick(m); open = false })
        }
    }
}

