package com.zhuji.note.data.repository

import androidx.sqlite.db.SimpleSQLiteQuery
import com.zhuji.note.data.local.db.FolderDao
import com.zhuji.note.data.local.db.FolderEntity
import com.zhuji.note.data.local.db.NoteDao
import com.zhuji.note.data.local.db.NoteEntity
import com.zhuji.note.data.local.db.NoteTagCrossRef
import com.zhuji.note.data.local.db.NoteWithTags
import com.zhuji.note.data.local.db.TagDao
import com.zhuji.note.data.local.db.TagEntity
import com.zhuji.note.domain.model.Folder
import com.zhuji.note.domain.model.Note
import com.zhuji.note.domain.model.NoteFilter
import com.zhuji.note.domain.model.NoteOrder
import com.zhuji.note.domain.model.Tag
import com.zhuji.note.domain.repository.FolderRepository
import com.zhuji.note.domain.repository.NoteRepository
import com.zhuji.note.domain.repository.NoteStats
import com.zhuji.note.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class NoteRepositoryImpl(
    private val noteDao: NoteDao,
    private val tagDao: TagDao,
) : NoteRepository {

    override fun observeNotes(filter: NoteFilter): Flow<List<Note>> {
        val sql = StringBuilder("SELECT * FROM notes WHERE deleted_at IS NULL AND archived = 0")
        val args = mutableListOf<Any>()
        if (filter.query.isNotBlank()) {
            sql.append(" AND (LOWER(title) LIKE ? OR LOWER(content) LIKE ?)")
            val q = "%${filter.query.lowercase().trim()}%"
            args.add(q); args.add(q)
        }
        if (filter.folderId != null) {
            sql.append(" AND folder_id = ?")
            args.add(filter.folderId)
        }
        if (filter.onlyPinned) sql.append(" AND pinned = 1")
        if (filter.onlyFavorite) sql.append(" AND favorite = 1")
        sql.append(
            when (filter.order) {
                NoteOrder.UpdatedDesc -> " ORDER BY pinned DESC, updated_at DESC"
                NoteOrder.UpdatedAsc -> " ORDER BY pinned DESC, updated_at ASC"
                NoteOrder.CreatedDesc -> " ORDER BY pinned DESC, created_at DESC"
                NoteOrder.Title -> " ORDER BY pinned DESC, LOWER(title) ASC"
                NoteOrder.WordCountDesc -> " ORDER BY pinned DESC, word_count DESC"
            }
        )
        val raw = SimpleSQLiteQuery(sql.toString(), args.toTypedArray())
        val source = noteDao.observeWithTagsRaw(raw)
        val tagFiltered = if (filter.tagId != null) {
            source.map { list -> list.filter { it.tags.any { t -> t.id == filter.tagId } } }
        } else source
        return tagFiltered.map { list -> list.map { it.toDomain() } }
    }

    override fun observeArchived(): Flow<List<Note>> = noteDao.observeArchived().map { list -> list.map { it.toDomain() } }
    override fun observeTrash(): Flow<List<Note>> = noteDao.observeTrash().map { list -> list.map { it.toDomain() } }
    override fun observeNote(id: Long): Flow<Note?> = noteDao.observeWithTags(id).map { it?.toDomain() }

    override fun observeStats(): Flow<NoteStats> = combine(
        noteDao.countActive(),
        noteDao.sumWordCount(),
        tagDao.observeAll(),
        noteDao.countPinned(),
        noteDao.countFavorite(),
    ) { count, words, tags, pinned, favorite ->
        NoteStats(
            totalNotes = count,
            totalWords = words ?: 0,
            totalTags = tags.size,
            pinnedCount = pinned ?: 0,
            favoriteCount = favorite ?: 0,
        )
    }

    override suspend fun upsert(note: Note): Long {
        val now = System.currentTimeMillis()
        val entity = note.toEntity().copy(updatedAt = now, wordCount = wordCount(note.content))
        val id = if (note.id == 0L) noteDao.insert(entity.copy(createdAt = now))
        else { noteDao.update(entity); note.id }
        noteDao.clearCross(id)
        note.tagIds.forEach { tagId -> noteDao.insertCross(NoteTagCrossRef(id, tagId)) }
        return id
    }

    override suspend fun setPinned(id: Long, pinned: Boolean) = noteDao.setPinned(id, pinned, System.currentTimeMillis())
    override suspend fun setFavorite(id: Long, favorite: Boolean) = noteDao.setFavorite(id, favorite, System.currentTimeMillis())
    override suspend fun setArchived(id: Long, archived: Boolean) = noteDao.setArchived(id, archived, System.currentTimeMillis())
    override suspend fun softDelete(id: Long) = noteDao.softDelete(id, System.currentTimeMillis())
    override suspend fun restore(id: Long) = noteDao.restore(id, System.currentTimeMillis())
    override suspend fun hardDelete(id: Long) = noteDao.hardDelete(id)
    override suspend fun purgeOldTrash(olderThanMillis: Long) = noteDao.purgeTrashBefore(System.currentTimeMillis() - olderThanMillis)
    override suspend fun setReminder(id: Long, ts: Long?) = noteDao.setReminder(id, ts, System.currentTimeMillis())
    override suspend fun assignTag(noteId: Long, tagId: Long) = noteDao.insertCross(NoteTagCrossRef(noteId, tagId))
    override suspend fun removeTag(noteId: Long, tagId: Long) = noteDao.deleteCross(NoteTagCrossRef(noteId, tagId))
}

class TagRepositoryImpl(private val dao: TagDao) : TagRepository {
    override fun observeAll(): Flow<List<Tag>> = dao.observeAll().map { list -> list.map { Tag(it.id, it.name, it.color, it.createdAt) } }
    override suspend fun upsert(tag: Tag): Long {
        val now = System.currentTimeMillis()
        val entity = TagEntity(id = tag.id, name = tag.name, color = tag.color, createdAt = if (tag.id == 0L) now else tag.createdAt)
        return if (tag.id == 0L) dao.insert(entity) else { dao.update(entity); tag.id }
    }
    override suspend fun delete(id: Long) = dao.delete(id)
}

class FolderRepositoryImpl(private val dao: FolderDao) : FolderRepository {
    override fun observeAll(): Flow<List<Folder>> = dao.observeAll().map { list -> list.map { Folder(it.id, it.name, it.emoji, it.sortOrder, it.createdAt) } }
    override suspend fun upsert(folder: Folder): Long {
        val now = System.currentTimeMillis()
        val entity = FolderEntity(folder.id, folder.name, folder.emoji, folder.sortOrder, if (folder.id == 0L) now else folder.createdAt)
        return dao.insert(entity)
    }
    override suspend fun delete(id: Long) = dao.delete(id)
}

internal fun wordCount(text: String): Int {
    if (text.isBlank()) return 0
    var c = 0
    var inWord = false
    text.forEach { ch ->
        when {
            ch.isLetterOrDigit() -> {
                if (ch in '\u4E00'..'\u9FFF') { c++; inWord = false }
                else if (!inWord) { c++; inWord = true }
            }
            else -> inWord = false
        }
    }
    return c
}

internal fun NoteEntity.toDomain(tagIds: List<Long> = emptyList()) = Note(
    id = id, title = title, content = content, color = color, pinned = pinned, favorite = favorite,
    archived = archived, deletedAt = deletedAt, reminderAt = reminderAt, folderId = folderId,
    createdAt = createdAt, updatedAt = updatedAt, wordCount = wordCount, tagIds = tagIds,
)

internal fun NoteWithTags.toDomain() = note.toDomain(tags.map { it.id })

internal fun Note.toEntity() = NoteEntity(
    id = id, title = title, content = content, color = color, pinned = pinned, favorite = favorite,
    archived = archived, deletedAt = deletedAt, reminderAt = reminderAt, folderId = folderId,
    createdAt = createdAt, updatedAt = updatedAt, wordCount = wordCount,
)
