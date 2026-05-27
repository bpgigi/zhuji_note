package com.zhuji.note.ui.screens.notes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuji.note.domain.model.Note
import com.zhuji.note.domain.model.NoteFilter
import com.zhuji.note.domain.model.NoteOrder
import com.zhuji.note.domain.repository.NoteStats
import com.zhuji.note.domain.repository.TagRepository
import com.zhuji.note.domain.usecase.DeleteNoteUseCase
import com.zhuji.note.domain.usecase.GetNotesUseCase
import com.zhuji.note.domain.usecase.PurgeTrashUseCase
import com.zhuji.note.domain.usecase.StatsUseCase
import com.zhuji.note.domain.usecase.ToggleFlagsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotesUiState(
    val filter: NoteFilter = NoteFilter(),
    val notes: List<Note> = emptyList(),
    val stats: NoteStats = NoteStats(0, 0, 0, 0, 0),
    val tagFilters: List<Pair<Long, String>> = emptyList(),
    val loading: Boolean = false,
    val errorMessage: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NotesViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getNotes: GetNotesUseCase,
    private val deleteNote: DeleteNoteUseCase,
    private val toggle: ToggleFlagsUseCase,
    private val stats: StatsUseCase,
    private val purge: PurgeTrashUseCase,
    tagRepository: TagRepository,
) : ViewModel() {

    private val filterState = MutableStateFlow(NoteFilter())
    val state: StateFlow<NotesUiState>

    init {
        val notesFlow = filterState.flatMapLatest { f -> getNotes(f) }
        val tagsFlow = tagRepository.observeAll()
        val statsFlow = stats()
        state = combine(filterState, notesFlow, statsFlow, tagsFlow) { f, list, st, tags ->
            NotesUiState(
                filter = f,
                notes = list,
                stats = st,
                tagFilters = tags.map { it.id to it.name },
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NotesUiState())

        viewModelScope.launch { runCatching { purge() } }
    }

    fun onQuery(q: String) = filterState.update { it.copy(query = q) }
    fun onTagFilter(tagId: Long?) = filterState.update { it.copy(tagId = tagId) }
    fun onPinnedOnly(only: Boolean) = filterState.update { it.copy(onlyPinned = only) }
    fun onFavoriteOnly(only: Boolean) = filterState.update { it.copy(onlyFavorite = only) }
    fun onOrder(order: NoteOrder) = filterState.update { it.copy(order = order) }

    fun togglePinned(id: Long, on: Boolean) = viewModelScope.launch { toggle(id, ToggleFlagsUseCase.Flag.Pinned, on) }
    fun toggleFavorite(id: Long, on: Boolean) = viewModelScope.launch { toggle(id, ToggleFlagsUseCase.Flag.Favorite, on) }
    fun softDelete(id: Long) = viewModelScope.launch { deleteNote(id) }
}
