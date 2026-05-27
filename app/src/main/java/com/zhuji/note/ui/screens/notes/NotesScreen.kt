package com.zhuji.note.ui.screens.notes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhuji.note.R
import com.zhuji.note.domain.model.Note
import com.zhuji.note.domain.model.NoteOrder
import com.zhuji.note.ui.common.AuroraBackground
import com.zhuji.note.ui.theme.Spacing
import com.zhuji.note.ui.theme.ZhujiCornerTokens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onOpen: (Long) -> Unit,
    onNew: () -> Unit,
    onSettings: () -> Unit,
    onStats: () -> Unit,
    onTrash: () -> Unit,
    onAi: () -> Unit,
    vm: NotesViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var orderMenu by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets.navigationBars,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNew,
                shape = ZhujiCornerTokens.FAB,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Rounded.Add, null) },
                text = { Text("新笔记", style = MaterialTheme.typography.labelLarge) },
            )
        }
    ) { padding: PaddingValues ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AuroraBackground(alpha = 0.05f)
            Column(Modifier.statusBarsPadding().padding(horizontal = Spacing.xl)) {
                Spacer(Modifier.height(Spacing.lg))
                TopBar(
                    title = "助记 · ZhujiNote",
                    subtitle = todayLabel(),
                    onAi = onAi,
                    onStats = onStats,
                    onTrash = onTrash,
                    onSettings = onSettings,
                    onSort = { orderMenu = true },
                )
                DropdownMenu(expanded = orderMenu, onDismissRequest = { orderMenu = false }) {
                    NoteOrder.values().forEach { o ->
                        DropdownMenuItem(text = { Text(o.displayName) }, onClick = {
                            vm.onOrder(o); orderMenu = false
                        })
                    }
                }
                Spacer(Modifier.height(Spacing.md))
                SearchField(state.filter.query, onChange = vm::onQuery)
                Spacer(Modifier.height(Spacing.sm))
                FilterStrip(
                    pinnedOnly = state.filter.onlyPinned,
                    favoriteOnly = state.filter.onlyFavorite,
                    activeTagId = state.filter.tagId,
                    tags = state.tagFilters,
                    onPinned = vm::onPinnedOnly,
                    onFavorite = vm::onFavoriteOnly,
                    onTag = vm::onTagFilter,
                )
                Spacer(Modifier.height(Spacing.sm))
                StatsRow(state.stats.totalNotes, state.stats.totalWords)
                Spacer(Modifier.height(Spacing.md))
                if (state.notes.isEmpty()) EmptyState(state.filter.query.isNotBlank())
                else NoteList(
                    notes = state.notes,
                    onOpen = onOpen,
                    onPin = { n -> vm.togglePinned(n.id, !n.pinned) },
                    onFavorite = { n -> vm.toggleFavorite(n.id, !n.favorite) },
                    onDelete = { n -> vm.softDelete(n.id) },
                )
            }
        }
    }
}

@Composable
private fun TopBar(
    title: String,
    subtitle: String,
    onAi: () -> Unit,
    onStats: () -> Unit,
    onTrash: () -> Unit,
    onSettings: () -> Unit,
    onSort: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onSort) { Icon(Icons.Outlined.Sort, contentDescription = "排序") }
        IconButton(onClick = onAi) { Icon(Icons.Outlined.AutoAwesome, contentDescription = "AI 助手", tint = MaterialTheme.colorScheme.primary) }
        IconButton(onClick = onStats) { Icon(Icons.Outlined.Insights, contentDescription = "统计") }
        IconButton(onClick = onTrash) { Icon(Icons.Outlined.Delete, contentDescription = "回收站") }
        IconButton(onClick = onSettings) { Icon(Icons.Outlined.Settings, contentDescription = "设置") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchField(value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        placeholder = { Text("搜索标题、内容、标签") },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        singleLine = true,
        shape = ZhujiCornerTokens.Input,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterStrip(
    pinnedOnly: Boolean,
    favoriteOnly: Boolean,
    activeTagId: Long?,
    tags: List<Pair<Long, String>>,
    onPinned: (Boolean) -> Unit,
    onFavorite: (Boolean) -> Unit,
    onTag: (Long?) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        FilterChip(
            selected = pinnedOnly,
            onClick = { onPinned(!pinnedOnly) },
            leadingIcon = { Icon(Icons.Outlined.PushPin, null, modifier = Modifier.size(16.dp)) },
            label = { Text("置顶") },
        )
        FilterChip(
            selected = favoriteOnly,
            onClick = { onFavorite(!favoriteOnly) },
            leadingIcon = { Icon(if (favoriteOnly) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder, null, modifier = Modifier.size(16.dp)) },
            label = { Text("收藏") },
        )
        if (activeTagId != null) {
            AssistChip(
                onClick = { onTag(null) },
                label = { Text("× 标签") },
                colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
            )
        }
        tags.take(3).forEach { (id, name) ->
            FilterChip(
                selected = activeTagId == id,
                onClick = { onTag(if (activeTagId == id) null else id) },
                label = { Text("# $name") },
                colors = FilterChipDefaults.filterChipColors(),
            )
        }
    }
}

@Composable
private fun StatsRow(notesCount: Int, words: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Pill(label = "$notesCount 篇")
        Pill(label = "$words 字")
    }
}

@Composable
private fun Pill(label: String) {
    Surface(
        shape = ZhujiCornerTokens.Chip,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xxs),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun NoteList(
    notes: List<Note>,
    onOpen: (Long) -> Unit,
    onPin: (Note) -> Unit,
    onFavorite: (Note) -> Unit,
    onDelete: (Note) -> Unit,
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        items(notes, key = { it.id }) { note ->
            NoteCard(note = note, onOpen = onOpen, onPin = onPin, onFavorite = onFavorite, onDelete = onDelete)
        }
        item { Spacer(Modifier.height(Spacing.huge)) }
    }
}

@Composable
private fun NoteCard(
    note: Note,
    onOpen: (Long) -> Unit,
    onPin: (Note) -> Unit,
    onFavorite: (Note) -> Unit,
    onDelete: (Note) -> Unit,
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 6 }),
        exit = fadeOut(),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(ZhujiCornerTokens.NoteCard)
                .clickable { onOpen(note.id) },
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = ZhujiCornerTokens.NoteCard,
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    Modifier
                        .padding(start = Spacing.sm, top = Spacing.lg, bottom = Spacing.lg)
                        .width(4.dp)
                        .heightIn(min = 36.dp)
                        .background(
                            color = if (note.pinned) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.tertiary,
                            shape = RoundedCornerShape(99.dp),
                        )
                )
                Column(
                    Modifier
                        .padding(Spacing.lg)
                        .weight(1f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = note.title.ifBlank { "未命名笔记" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (note.favorite) {
                            Icon(Icons.Outlined.Favorite, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    if (note.content.isNotBlank()) {
                        Spacer(Modifier.height(Spacing.xs))
                        Text(
                            text = note.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(Modifier.height(Spacing.sm))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = formatTimestamp(note.updatedAt),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { onPin(note) }) {
                            Icon(Icons.Outlined.PushPin, null, tint = if (note.pinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { onFavorite(note) }) {
                            Icon(if (note.favorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder, null, tint = if (note.favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { onDelete(note) }) {
                            Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(searching: Boolean) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(Modifier.height(Spacing.lg))
            Text(
                text = if (searching) "没有匹配的笔记" else "写下你的第一段思考。",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                text = "按右下角 + 来开始；之后可以让 AI 助记一下。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun todayLabel(): String {
    val sdf = SimpleDateFormat("yyyy 年 M 月 d 日 EEEE", Locale.CHINA)
    return sdf.format(Date())
}

private fun formatTimestamp(ts: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - ts
    return when {
        diff < 60_000 -> "刚刚"
        diff < 3600_000 -> "${diff / 60_000} 分钟前"
        diff < 86400_000 -> "${diff / 3600_000} 小时前"
        diff < 7 * 86400_000 -> "${diff / 86400_000} 天前"
        else -> SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Date(ts))
    }
}
