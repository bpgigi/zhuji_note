package com.zhuji.note.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhuji.note.domain.repository.NoteStats
import com.zhuji.note.domain.usecase.StatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(stats: StatsUseCase) : ViewModel() {
    val state: StateFlow<NoteStats> = stats().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000),
        NoteStats(0, 0, 0, 0, 0)
    )
}
