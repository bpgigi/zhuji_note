package com.zhuji.note.domain.usecase

import com.zhuji.note.domain.model.Note
import com.zhuji.note.domain.model.NoteFilter
import com.zhuji.note.domain.repository.NoteRepository
import com.zhuji.note.domain.repository.NoteStats
import kotlinx.coroutines.flow.Flow

class GetNotesUseCase(private val repo: NoteRepository) {
    operator fun invoke(filter: NoteFilter): Flow<List<Note>> = repo.observeNotes(filter)
}

class GetNoteUseCase(private val repo: NoteRepository) {
    operator fun invoke(id: Long): Flow<Note?> = repo.observeNote(id)
}

class SaveNoteUseCase(private val repo: NoteRepository) {
    suspend operator fun invoke(note: Note): Result<Long> = runCatching {
        require(note.title.isNotBlank() || note.content.isNotBlank()) { "笔记内容不能为空" }
        repo.upsert(note)
    }
}

class DeleteNoteUseCase(private val repo: NoteRepository) {
    suspend operator fun invoke(id: Long, hard: Boolean = false) =
        if (hard) repo.hardDelete(id) else repo.softDelete(id)
}

class ToggleFlagsUseCase(private val repo: NoteRepository) {
    enum class Flag { Pinned, Favorite, Archived }
    suspend operator fun invoke(id: Long, flag: Flag, on: Boolean) = when (flag) {
        Flag.Pinned -> repo.setPinned(id, on)
        Flag.Favorite -> repo.setFavorite(id, on)
        Flag.Archived -> repo.setArchived(id, on)
    }
}

class ReminderUseCase(private val repo: NoteRepository) {
    suspend operator fun invoke(id: Long, ts: Long?) = repo.setReminder(id, ts)
}

class StatsUseCase(private val repo: NoteRepository) {
    operator fun invoke(): Flow<NoteStats> = repo.observeStats()
}

class PurgeTrashUseCase(private val repo: NoteRepository) {
    suspend operator fun invoke() = repo.purgeOldTrash()
}
