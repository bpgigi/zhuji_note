package com.zhuji.note.data.local.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Transaction
    @Query("SELECT * FROM notes WHERE deleted_at IS NULL AND archived = 0 ORDER BY pinned DESC, updated_at DESC")
    fun observeAllWithTags(): Flow<List<NoteWithTags>>

    @Transaction
    @RawQuery(observedEntities = [NoteEntity::class, TagEntity::class, NoteTagCrossRef::class])
    fun observeWithTagsRaw(query: SupportSQLiteQuery): Flow<List<NoteWithTags>>

    @Query("SELECT * FROM notes WHERE deleted_at IS NULL AND archived = 0 ORDER BY pinned DESC, updated_at DESC")
    fun observeAll(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE archived = 1 AND deleted_at IS NULL ORDER BY updated_at DESC")
    fun observeArchived(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE deleted_at IS NOT NULL ORDER BY deleted_at DESC")
    fun observeTrash(): Flow<List<NoteEntity>>

    @Transaction
    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    fun observeWithTags(id: Long): Flow<NoteWithTags?>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): NoteEntity?

    @Transaction
    @Query("""
        SELECT notes.* FROM notes
        JOIN notes_fts ON notes.id = notes_fts.docid
        WHERE notes_fts MATCH :match
          AND notes.deleted_at IS NULL AND notes.archived = 0
        ORDER BY notes.pinned DESC, notes.updated_at DESC
    """)
    fun searchFts(match: String): Flow<List<NoteWithTags>>

    @Transaction
    @Query("""
        SELECT * FROM notes
        WHERE deleted_at IS NULL AND archived = 0
          AND (LOWER(title) LIKE '%' || LOWER(:q) || '%'
            OR LOWER(content) LIKE '%' || LOWER(:q) || '%')
        ORDER BY pinned DESC, updated_at DESC
    """)
    fun searchLike(q: String): Flow<List<NoteWithTags>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity): Long

    @Update
    suspend fun update(note: NoteEntity)

    @Query("UPDATE notes SET pinned = :pinned, updated_at = :now WHERE id = :id")
    suspend fun setPinned(id: Long, pinned: Boolean, now: Long)

    @Query("UPDATE notes SET favorite = :favorite, updated_at = :now WHERE id = :id")
    suspend fun setFavorite(id: Long, favorite: Boolean, now: Long)

    @Query("UPDATE notes SET archived = :archived, updated_at = :now WHERE id = :id")
    suspend fun setArchived(id: Long, archived: Boolean, now: Long)

    @Query("UPDATE notes SET deleted_at = :ts WHERE id = :id")
    suspend fun softDelete(id: Long, ts: Long)

    @Query("UPDATE notes SET deleted_at = NULL, updated_at = :now WHERE id = :id")
    suspend fun restore(id: Long, now: Long)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun hardDelete(id: Long)

    @Query("DELETE FROM notes WHERE deleted_at IS NOT NULL AND deleted_at < :before")
    suspend fun purgeTrashBefore(before: Long)

    @Query("UPDATE notes SET reminder_at = :ts, updated_at = :now WHERE id = :id")
    suspend fun setReminder(id: Long, ts: Long?, now: Long)

    @Query("SELECT COUNT(*) FROM notes WHERE deleted_at IS NULL")
    fun countActive(): Flow<Int>

    @Query("SELECT SUM(word_count) FROM notes WHERE deleted_at IS NULL")
    fun sumWordCount(): Flow<Int?>

    @Query("SELECT SUM(CASE WHEN pinned = 1 THEN 1 ELSE 0 END) FROM notes WHERE deleted_at IS NULL")
    fun countPinned(): Flow<Int?>

    @Query("SELECT SUM(CASE WHEN favorite = 1 THEN 1 ELSE 0 END) FROM notes WHERE deleted_at IS NULL")
    fun countFavorite(): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCross(ref: NoteTagCrossRef)

    @Delete
    suspend fun deleteCross(ref: NoteTagCrossRef)

    @Query("DELETE FROM note_tag_cross WHERE note_id = :noteId")
    suspend fun clearCross(noteId: Long)
}

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY name")
    fun observeAll(): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tag: TagEntity): Long

    @Update
    suspend fun update(tag: TagEntity)

    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM tags WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TagEntity?

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): TagEntity?

    @Query("SELECT COUNT(*) FROM note_tag_cross WHERE tag_id = :tagId")
    fun usageCount(tagId: Long): Flow<Int>
}

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders ORDER BY sort_order, name")
    fun observeAll(): Flow<List<FolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(folder: FolderEntity): Long

    @Update
    suspend fun update(folder: FolderEntity)

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM folders WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): FolderEntity?
}
