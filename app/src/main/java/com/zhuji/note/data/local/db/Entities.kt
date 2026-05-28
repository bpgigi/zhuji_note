package com.zhuji.note.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Fts4
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val content: String,
    val color: Int = 0,
    val pinned: Boolean = false,
    val favorite: Boolean = false,
    val archived: Boolean = false,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long? = null,
    @ColumnInfo(name = "reminder_at") val reminderAt: Long? = null,
    @ColumnInfo(name = "folder_id") val folderId: Long? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "word_count") val wordCount: Int = 0,
)

@Entity(tableName = "tags", indices = [Index(value = ["name"], unique = true)])
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val color: Int = 0,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)

@Entity(
    tableName = "note_tag_cross",
    primaryKeys = ["note_id", "tag_id"],
    foreignKeys = [
        ForeignKey(entity = NoteEntity::class, parentColumns = ["id"], childColumns = ["note_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TagEntity::class, parentColumns = ["id"], childColumns = ["tag_id"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("tag_id")]
)
data class NoteTagCrossRef(
    @ColumnInfo(name = "note_id") val noteId: Long,
    @ColumnInfo(name = "tag_id") val tagId: Long,
)

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val emoji: String,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)

@Entity(tableName = "notes_fts")
@Fts4(contentEntity = NoteEntity::class)
data class NoteFts(
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "content") val content: String,
)
