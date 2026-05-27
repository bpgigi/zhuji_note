package com.zhuji.note.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [NoteEntity::class, TagEntity::class, NoteTagCrossRef::class, FolderEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class ZhujiDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun tagDao(): TagDao
    abstract fun folderDao(): FolderDao

    companion object { const val NAME = "zhuji.db" }
}
