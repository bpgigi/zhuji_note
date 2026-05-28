package com.zhuji.note.stage2

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.zhuji.note.data.local.db.NoteDao
import com.zhuji.note.data.local.db.TagDao
import com.zhuji.note.data.repository.NoteRepositoryImpl
import com.zhuji.note.domain.model.Note
import com.zhuji.note.domain.model.NoteFilter
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RepositoryEdgeCasesTest {
    private val noteDao = mockk<NoteDao>(relaxUnitFun = true)
    private val tagDao = mockk<TagDao>(relaxUnitFun = true)

    private fun repo() = NoteRepositoryImpl(noteDao, tagDao)

    @Test fun `empty list returns empty`() = runTest {
        every { noteDao.observeWithTagsRaw(any()) } returns flowOf(emptyList())
        every { tagDao.observeAll() } returns flowOf(emptyList())
        repo().observeNotes(NoteFilter()).test {
            assertThat(awaitItem()).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test fun `softDelete calls dao with timestamp`() = runTest {
        coJustRun { noteDao.softDelete(any(), any()) }
        repo().softDelete(1L)
        coVerify { noteDao.softDelete(1L, any()) }
    }

    @Test fun `restore calls dao`() = runTest {
        coJustRun { noteDao.restore(any(), any()) }
        repo().restore(5L)
        coVerify { noteDao.restore(5L, any()) }
    }

    @Test fun `hardDelete calls dao`() = runTest {
        coJustRun { noteDao.hardDelete(any()) }
        repo().hardDelete(3L)
        coVerify { noteDao.hardDelete(3L) }
    }

    @Test fun `upsert with id 0 inserts`() = runTest {
        coEvery { noteDao.insert(any()) } returns 99L
        coJustRun { noteDao.clearCross(any()) }
        coJustRun { noteDao.insertCross(any()) }
        val id = repo().upsert(Note(id = 0, title = "t", content = "c"))
        assertThat(id).isEqualTo(99L)
        coVerify(exactly = 1) { noteDao.insert(any()) }
    }

    @Test fun `upsert with id greater than 0 updates`() = runTest {
        coJustRun { noteDao.update(any()) }
        coJustRun { noteDao.clearCross(any()) }
        coJustRun { noteDao.insertCross(any()) }
        val id = repo().upsert(Note(id = 5, title = "t", content = "c"))
        assertThat(id).isEqualTo(5L)
        coVerify(exactly = 1) { noteDao.update(any()) }
    }

    @Test fun `observeTrash delegates to dao`() = runTest {
        every { noteDao.observeTrash() } returns flowOf(emptyList())
        repo().observeTrash().test {
            assertThat(awaitItem()).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
