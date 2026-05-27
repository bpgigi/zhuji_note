package com.zhuji.note.data.repository

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
        val source = if (filter.query.isNotBlank()) noteDao.search(filter.query.trim()).asWithTagsLike(noteDao)
        else noteDao.observeAllWithTags()
        return source.map { list ->
            list.asSequence()
                .filter { if (filter.folderId != null) it.note.folderId == filter.folderId else true }
                .filter { if (filter.tagId != null) it.tags.any { t -> t.id == filter.tagId } else true }
                .filter { if (filter.onlyPinned) it.note.pinned else true }
                .filter { if (filter.onlyFavorite) it.note.favorite else true }
                .map { it.toDomain() }
                .let { seq ->
                    when (filter.order) {
                        NoteOrder.UpdatedDesc -> seq.sortedWith(compareByDescending<Note> { it.pinned }.thenByDescending { it.updatedAt })
                        NoteOrder.UpdatedAsc -> seq.sortedBy { it.updatedAt }
                        NoteOrder.CreatedDesc -> seq.sortedByDescending { it.createdAt }
                        NoteOrder.Title -> seq.sortedBy { it.title.lowercase() }
                        NoteOrder.WordCountDesc -> seq.sortedByDescending { it.wordCount }
                    }
                }
                .toList()
        }
    }

    override fun observeArchived(): Flow<List<Note>> = noteDao.observeArchived().map { list -> list.map { it.toDomain() } }
    override fun observeTrash(): Flow<List<Note>> = noteDao.observeTrash().map { list -> list.map { it.toDomain() } }
    override fun observeNote(id: Long): Flow<Note?> = noteDao.observeWithTags(id).map { it?.toDomain() }

    override fun observeStats(): Flow<NoteStats> = combine(
        noteDao.countActive(),
        noteDao.sumWordCount(),
        tagDao.observeAll(),
        noteDao.observeAllWithTags(),
    ) { count, words, tags, withTags ->
        NoteStats(
            totalNotes = count,
            totalWords = words ?: 0,
            totalTags = tags.size,
            pinnedCount = withTags.count { it.note.pinned },
            favoriteCount = withTags.count { it.note.favorite },
        )
    }

    override suspend fun upsert(note: Note): Long {
        val now = System.currentTimeMillis()
        val entity = note.toEntity().copy(updatedAt = now, wordCount = wordCount(note.content))
        val id = if (note.id == 0L) noteDao.insert(entity.copy(createdAt = now))
        else { noteDao.update(entity); note.id }
        // refresh tag links
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
    createdAt = createdAt, updatedAt = updatedAt, wordCount = wordCount, tagIds = tagIds
)

internal fun NoteWithTags.toDomain() = note.toDomain(tags.map { it.id })

internal fun Note.toEntity() = NoteEntity(
    id = id, title = title, content = content, color = color, pinned = pinned, favorite = favorite,
    archived = archived, deletedAt = deletedAt, reminderAt = reminderAt, folderId = folderId,
    createdAt = createdAt, updatedAt = updatedAt, wordCount = wordCount,
)

// helper: search() returns NoteEntity not NoteWithTags; wrap into a flow of NoteWithTags by joining tags from observeAllWithTags
private fun Flow<List<NoteEntity>>.asWithTagsLike(noteDao: NoteDao): Flow<List<NoteWithTags>> =
    combine(this, noteDao.observeAllWithTags()) { hits, all ->
        val tagsById = all.associateBy { it.note.id }
        hits.map { e -> tagsById[e.id] ?: NoteWithTags(e, emptyList()) }
    }
