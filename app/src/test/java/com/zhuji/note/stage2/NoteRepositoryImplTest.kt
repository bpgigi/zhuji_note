package com.zhuji.note.stage2

import com.google.common.truth.Truth.assertThat
import com.zhuji.note.data.repository.NoteRepositoryImpl
import com.zhuji.note.domain.model.Note
import com.zhuji.note.domain.model.NoteFilter
import com.zhuji.note.domain.model.NoteOrder
import com.zhuji.note.data.local.db.NoteDao
import com.zhuji.note.data.local.db.NoteEntity
import com.zhuji.note.data.local.db.NoteWithTags
import com.zhuji.note.data.local.db.TagDao
import com.zhuji.note.data.local.db.TagEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coJustRun
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteRepositoryImplTest {

    private val noteDao = mockk<NoteDao>(relaxUnitFun = true)
    private val tagDao = mockk<TagDao>(relaxUnitFun = true)

    @Test
    fun `observeNotes orders by pinned then updated desc`() = runTest {
        val n1 = NoteEntity(id = 1, title = "A", content = "", pinned = false, createdAt = 0, updatedAt = 100)
        val n2 = NoteEntity(id = 2, title = "B", content = "", pinned = true, createdAt = 0, updatedAt = 50)
        val n3 = NoteEntity(id = 3, title = "C", content = "", pinned = false, createdAt = 0, updatedAt = 200)
        every { noteDao.observeAllWithTags() } returns flowOf(listOf(
            NoteWithTags(n1, emptyList()),
            NoteWithTags(n2, emptyList()),
            NoteWithTags(n3, emptyList()),
        ))
        every { tagDao.observeAll() } returns flowOf(emptyList())
        val repo = NoteRepositoryImpl(noteDao, tagDao)
        val list: List<Note> = repo.observeNotes(NoteFilter(order = NoteOrder.UpdatedDesc)).first()
        assertThat(list.map { it.id }).containsExactly(2L, 3L, 1L).inOrder()
    }

    @Test
    fun `observeNotes filter onlyFavorite`() = runTest {
        val n1 = NoteEntity(id = 1, title = "x", content = "", favorite = true, createdAt = 0, updatedAt = 100)
        val n2 = NoteEntity(id = 2, title = "y", content = "", favorite = false, createdAt = 0, updatedAt = 90)
        every { noteDao.observeAllWithTags() } returns flowOf(listOf(NoteWithTags(n1, emptyList()), NoteWithTags(n2, emptyList())))
        every { tagDao.observeAll() } returns flowOf(emptyList())
        val repo = NoteRepositoryImpl(noteDao, tagDao)
        val list = repo.observeNotes(NoteFilter(onlyFavorite = true)).first()
        assertThat(list.map { it.id }).containsExactly(1L)
    }

    @Test
    fun `upsert inserts new note and refreshes tags`() = runTest {
        coEvery { noteDao.insert(any()) } returns 42L
        coJustRun { noteDao.clearCross(any()) }
        coJustRun { noteDao.insertCross(any()) }
        val repo = NoteRepositoryImpl(noteDao, tagDao)
        val newId = repo.upsert(Note(title = "t", content = "c", tagIds = listOf(7L, 8L)))
        assertThat(newId).isEqualTo(42L)
        coVerify(exactly = 1) { noteDao.insert(any()) }
        coVerify(exactly = 1) { noteDao.clearCross(42L) }
        coVerify(exactly = 2) { noteDao.insertCross(any()) }
    }

    @Test
    fun `upsert updates existing note`() = runTest {
        coJustRun { noteDao.update(any()) }
        coJustRun { noteDao.clearCross(any()) }
        val repo = NoteRepositoryImpl(noteDao, tagDao)
        val id = repo.upsert(Note(id = 99L, title = "t", content = "c"))
        assertThat(id).isEqualTo(99L)
        coVerify(exactly = 1) { noteDao.update(any()) }
        coVerify(exactly = 0) { noteDao.insert(any()) }
    }

    @Test
    fun `softDelete forwards to dao`() = runTest {
        coJustRun { noteDao.softDelete(any(), any()) }
        val repo = NoteRepositoryImpl(noteDao, tagDao)
        repo.softDelete(5L)
        coVerify(exactly = 1) { noteDao.softDelete(5L, any()) }
    }

    @Test
    fun `setPinned forwards to dao`() = runTest {
        coJustRun { noteDao.setPinned(any(), any(), any()) }
        val repo = NoteRepositoryImpl(noteDao, tagDao)
        repo.setPinned(3L, true)
        coVerify(exactly = 1) { noteDao.setPinned(3L, true, any()) }
    }
}
