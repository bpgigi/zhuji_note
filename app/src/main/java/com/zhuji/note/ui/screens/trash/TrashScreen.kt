package com.zhuji.note.ui.screens.trash

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.RestoreFromTrash
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.zhuji.note.domain.model.Note
import com.zhuji.note.domain.repository.NoteRepository
import com.zhuji.note.ui.theme.Spacing
import com.zhuji.note.ui.theme.ZhujiCornerTokens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(onBack: () -> Unit, vm: TrashViewModel = hiltViewModel()) {
    val notes by vm.state.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        topBar = {
            TopAppBar(
                title = { Text("回收站", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            if (notes.isEmpty()) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(Spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(Modifier.height(Spacing.huge))
                    Text("回收站为空。", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(Spacing.sm))
                    Text("被删除的笔记会保留 30 天。", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.lg)) {
                    items(notes, key = { it.id }) { note ->
                        TrashRow(note = note, onRestore = {
                            vm.restore(it)
                            scope.launch { snack.showSnackbar("已还原") }
                        }, onPurge = {
                            vm.purge(it)
                            scope.launch { snack.showSnackbar("已永久删除") }
                        })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrashRow(note: Note, onRestore: (Long) -> Unit, onPurge: (Long) -> Unit) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> { onRestore(note.id); true }
                SwipeToDismissBoxValue.EndToStart -> { onPurge(note.id); true }
                else -> false
            }
        }
    )
    SwipeToDismissBox(
        state = state,
        backgroundContent = {
            val color = when (state.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.tertiary
                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
            Box(
                Modifier.fillMaxSize().background(color).padding(horizontal = Spacing.lg),
                contentAlignment = if (state.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                    Alignment.CenterEnd else Alignment.CenterStart,
            ) {
                val icon = if (state.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                    Icons.Outlined.DeleteForever else Icons.Outlined.RestoreFromTrash
                val label = if (state.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                    "永久删除" else "还原"
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.padding(start = Spacing.xs))
                    Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        },
    ) {
        Surface(
            shape = ZhujiCornerTokens.NoteCard,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xxs),
        ) {
            ListItem(
                headlineContent = { Text(note.title.ifBlank { "未命名" }, style = MaterialTheme.typography.titleMedium) },
                supportingContent = { Text(note.content.take(80), maxLines = 2, style = MaterialTheme.typography.bodySmall) },
                trailingContent = {
                    Row {
                        AssistChip(
                            onClick = { onRestore(note.id) },
                            label = { Text("还原") },
                            leadingIcon = { Icon(Icons.Outlined.RestoreFromTrash, null) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                        )
                        Spacer(Modifier.padding(start = Spacing.xs))
                        AssistChip(
                            onClick = { onPurge(note.id) },
                            label = { Text("彻底删") },
                            leadingIcon = { Icon(Icons.Outlined.DeleteForever, null) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        )
                    }
                },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            )
        }
    }
}

@HiltViewModel
class TrashViewModel @Inject constructor(private val repo: NoteRepository) : ViewModel() {
    val state: StateFlow<List<Note>> = repo.observeTrash()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun restore(id: Long) = viewModelScope.launch { repo.restore(id) }
    fun purge(id: Long) = viewModelScope.launch { repo.hardDelete(id) }
}
