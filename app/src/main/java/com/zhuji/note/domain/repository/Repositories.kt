package com.zhuji.note.domain.repository

import com.zhuji.note.domain.model.Folder
import com.zhuji.note.domain.model.Note
import com.zhuji.note.domain.model.NoteFilter
import com.zhuji.note.domain.model.Tag
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun observeNotes(filter: NoteFilter): Flow<List<Note>>
    fun observeArchived(): Flow<List<Note>>
    fun observeTrash(): Flow<List<Note>>
    fun observeNote(id: Long): Flow<Note?>
    fun observeStats(): Flow<NoteStats>
    suspend fun upsert(note: Note): Long
    suspend fun setPinned(id: Long, pinned: Boolean)
    suspend fun setFavorite(id: Long, favorite: Boolean)
    suspend fun setArchived(id: Long, archived: Boolean)
    suspend fun softDelete(id: Long)
    suspend fun restore(id: Long)
    suspend fun hardDelete(id: Long)
    suspend fun purgeOldTrash(olderThanMillis: Long = 30L * 24 * 3600 * 1000)
    suspend fun setReminder(id: Long, ts: Long?)
    suspend fun assignTag(noteId: Long, tagId: Long)
    suspend fun removeTag(noteId: Long, tagId: Long)
}

interface TagRepository {
    fun observeAll(): Flow<List<Tag>>
    suspend fun upsert(tag: Tag): Long
    suspend fun delete(id: Long)
}

interface FolderRepository {
    fun observeAll(): Flow<List<Folder>>
    suspend fun upsert(folder: Folder): Long
    suspend fun delete(id: Long)
}

data class NoteStats(
    val totalNotes: Int,
    val totalWords: Int,
    val totalTags: Int,
    val pinnedCount: Int,
    val favoriteCount: Int,
)
