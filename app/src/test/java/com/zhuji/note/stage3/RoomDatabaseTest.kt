package com.zhuji.note.stage3

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.zhuji.note.data.local.db.NoteEntity
import com.zhuji.note.data.local.db.NoteTagCrossRef
import com.zhuji.note.data.local.db.TagEntity
import com.zhuji.note.data.local.db.ZhujiDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class RoomDatabaseTest {

    private lateinit var db: ZhujiDatabase

    @Before fun create() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ZhujiDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After fun tearDown() { db.close() }

    @Test
    fun `note insert and observe`() = runTest {
        val now = System.currentTimeMillis()
        val id = db.noteDao().insert(NoteEntity(title = "x", content = "y", createdAt = now, updatedAt = now))
        val list = db.noteDao().observeAll().first()
        assertThat(list.first().id).isEqualTo(id)
        assertThat(list.first().title).isEqualTo("x")
    }

    @Test
    fun `tag and note cross ref relation`() = runTest {
        val now = System.currentTimeMillis()
        val noteId = db.noteDao().insert(NoteEntity(title = "n", content = "c", createdAt = now, updatedAt = now))
        val tagId = db.tagDao().insert(TagEntity(name = "想法", createdAt = now))
        db.noteDao().insertCross(NoteTagCrossRef(noteId, tagId))
        val withTags = db.noteDao().observeAllWithTags().first()
        assertThat(withTags).hasSize(1)
        assertThat(withTags.first().tags.map { it.id }).containsExactly(tagId)
    }

    @Test
    fun `soft delete moves to trash`() = runTest {
        val now = System.currentTimeMillis()
        val id = db.noteDao().insert(NoteEntity(title = "t", content = "c", createdAt = now, updatedAt = now))
        db.noteDao().softDelete(id, now)
        assertThat(db.noteDao().observeAll().first()).isEmpty()
        assertThat(db.noteDao().observeTrash().first()).hasSize(1)
    }

    @Test
    fun `restore moves back to active`() = runTest {
        val now = System.currentTimeMillis()
        val id = db.noteDao().insert(NoteEntity(title = "t", content = "c", createdAt = now, updatedAt = now))
        db.noteDao().softDelete(id, now)
        db.noteDao().restore(id, now)
        assertThat(db.noteDao().observeAll().first().map { it.id }).contains(id)
    }

    @Test
    fun `search matches title and content`() = runTest {
        val now = System.currentTimeMillis()
        db.noteDao().insert(NoteEntity(title = "Compose", content = "is fun", createdAt = now, updatedAt = now))
        db.noteDao().insert(NoteEntity(title = "Java", content = "boring", createdAt = now, updatedAt = now))
        val hits = db.noteDao().search("Compose").first()
        assertThat(hits.map { it.title }).containsExactly("Compose")
    }

    @Test
    fun `count and word count aggregations`() = runTest {
        val now = System.currentTimeMillis()
        db.noteDao().insert(NoteEntity(title = "a", content = "b", wordCount = 3, createdAt = now, updatedAt = now))
        db.noteDao().insert(NoteEntity(title = "c", content = "d", wordCount = 5, createdAt = now, updatedAt = now))
        assertThat(db.noteDao().countActive().first()).isEqualTo(2)
        assertThat(db.noteDao().sumWordCount().first()).isEqualTo(8)
    }
}
