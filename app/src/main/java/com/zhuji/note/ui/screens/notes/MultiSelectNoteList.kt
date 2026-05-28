package com.zhuji.note.ui.screens.notes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zhuji.note.domain.model.Note
import com.zhuji.note.ui.common.pressableScale
import com.zhuji.note.ui.theme.Spacing
import com.zhuji.note.ui.theme.ZhujiCornerTokens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MultiSelectNoteList(
    notes: List<Note>,
    onNoteClick: (Long) -> Unit,
    onBatchDelete: (List<Long>) -> Unit,
    onBatchTag: (List<Long>) -> Unit,
    onNewNote: () -> Unit,
) {
    var selectionMode by remember { mutableStateOf(false) }
    val selected = remember { mutableStateListOf<Long>() }
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        topBar = {
            if (selectionMode) {
                TopAppBar(
                    title = { Text("已选 ${selected.size} 项") },
                    navigationIcon = {
                        IconButton(onClick = { selectionMode = false; selected.clear() }) {
                            Icon(Icons.Outlined.Close, "取消")
                        }
                    },
                    actions = {
                        IconButton(onClick = { selected.clear(); selected.addAll(notes.map { it.id }) }) {
                            Icon(Icons.Outlined.SelectAll, "全选")
                        }
                        IconButton(onClick = {
                            onBatchTag(selected.toList())
                            scope.launch { snack.showSnackbar("已批量打标签") }
                        }) { Icon(Icons.Outlined.Label, "批量标签") }
                        IconButton(onClick = {
                            onBatchDelete(selected.toList())
                            selectionMode = false; selected.clear()
                            scope.launch { snack.showSnackbar("已批量删除") }
                        }) { Icon(Icons.Outlined.Delete, "批量删除") }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                )
            }
        },
        floatingActionButton = {
            AnimatedVisibility(!selectionMode, enter = fadeIn(), exit = fadeOut()) {
                FloatingActionButton(onClick = onNewNote, containerColor = MaterialTheme.colorScheme.primary) {
                    Icon(Icons.Outlined.Add, "新笔记")
                }
            }
        },
    ) { padding ->
        LazyColumn(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            items(notes, key = { it.id }) { note ->
                val isSelected = note.id in selected
                Surface(
                    shape = ZhujiCornerTokens.NoteCard,
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .pressableScale()
                        .combinedClickable(
                            onClick = {
                                if (selectionMode) {
                                    if (isSelected) selected.remove(note.id) else selected.add(note.id)
                                } else onNoteClick(note.id)
                            },
                            onLongClick = {
                                if (!selectionMode) { selectionMode = true; selected.add(note.id) }
                            },
                        ),
                ) {
                    Row(Modifier.padding(Spacing.lg), verticalAlignment = Alignment.CenterVertically) {
                        if (selectionMode) {
                            Checkbox(checked = isSelected, onCheckedChange = {
                                if (it) selected.add(note.id) else selected.remove(note.id)
                            })
                            Spacer(Modifier.width(Spacing.sm))
                        }
                        Column(Modifier.weight(1f)) {
                            Text(
                                note.title.ifBlank { "未命名" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                note.content.take(100),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        if (note.pinned) {
                            Icon(Icons.Outlined.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}
