package com.zhuji.note.stage2

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.zhuji.note.ai.AiAction
import com.zhuji.note.ai.ChatMessage
import com.zhuji.note.ai.ChatRequest
import com.zhuji.note.ai.DeepSeekClient
import com.zhuji.note.ai.ChatChoice
import com.zhuji.note.ai.ChatDelta
import com.zhuji.note.ai.ChatResponse
import com.zhuji.note.data.local.preferences.ThemeMode
import com.zhuji.note.data.local.preferences.UserPreferencesDataStore
import com.zhuji.note.data.repository.NoteRepositoryImpl
import com.zhuji.note.data.local.db.NoteDao
import com.zhuji.note.data.local.db.NoteEntity
import com.zhuji.note.data.local.db.NoteWithTags
import com.zhuji.note.data.local.db.TagDao
import com.zhuji.note.data.local.db.TagEntity
import com.zhuji.note.domain.model.Note
import com.zhuji.note.domain.model.NoteFilter
import com.zhuji.note.domain.model.NoteOrder
import com.zhuji.note.domain.repository.NoteStats
import com.zhuji.note.domain.repository.TagRepository
import com.zhuji.note.domain.usecase.GetNotesUseCase
import com.zhuji.note.domain.usecase.PurgeTrashUseCase
import com.zhuji.note.domain.usecase.SaveNoteUseCase
import com.zhuji.note.domain.usecase.StatsUseCase
import com.zhuji.note.domain.usecase.ToggleFlagsUseCase
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExtendedRepositoryTest {

    private val noteDao = mockk<NoteDao>(relaxUnitFun = true)
    private val tagDao = mockk<TagDao>(relaxUnitFun = true)

    @Test
    fun `observeNotes order by title sorts case-insensitive`() = runTest {
        val now = 0L
        val notes = listOf(
            NoteWithTags(NoteEntity(id = 1, title = "Banana", content = "", createdAt = now, updatedAt = now), emptyList()),
            NoteWithTags(NoteEntity(id = 2, title = "apple", content = "", createdAt = now, updatedAt = now), emptyList()),
            NoteWithTags(NoteEntity(id = 3, title = "cherry", content = "", createdAt = now, updatedAt = now), emptyList()),
        )
        every { noteDao.observeAllWithTags() } returns flowOf(notes)
        every { tagDao.observeAll() } returns flowOf(emptyList())
        val repo = NoteRepositoryImpl(noteDao, tagDao)
        val list = repo.observeNotes(NoteFilter(order = NoteOrder.Title)).first()
        assertThat(list.map { it.title }).containsExactly("apple", "Banana", "cherry").inOrder()
    }

    @Test
    fun `observeNotes order by word count desc`() = runTest {
        val notes = listOf(
            NoteWithTags(NoteEntity(id = 1, title = "a", content = "", wordCount = 5, createdAt = 0, updatedAt = 0), emptyList()),
            NoteWithTags(NoteEntity(id = 2, title = "b", content = "", wordCount = 50, createdAt = 0, updatedAt = 0), emptyList()),
            NoteWithTags(NoteEntity(id = 3, title = "c", content = "", wordCount = 20, createdAt = 0, updatedAt = 0), emptyList()),
        )
        every { noteDao.observeAllWithTags() } returns flowOf(notes)
        every { tagDao.observeAll() } returns flowOf(emptyList())
        val repo = NoteRepositoryImpl(noteDao, tagDao)
        val list = repo.observeNotes(NoteFilter(order = NoteOrder.WordCountDesc)).first()
        assertThat(list.map { it.id }).containsExactly(2L, 3L, 1L).inOrder()
    }

    @Test
    fun `observeNotes filter by tag id`() = runTest {
        val tag1 = TagEntity(id = 5, name = "x", color = 0, createdAt = 0)
        val list = listOf(
            NoteWithTags(NoteEntity(id = 1, title = "a", content = "", createdAt = 0, updatedAt = 0), listOf(tag1)),
            NoteWithTags(NoteEntity(id = 2, title = "b", content = "", createdAt = 0, updatedAt = 0), emptyList()),
        )
        every { noteDao.observeAllWithTags() } returns flowOf(list)
        every { tagDao.observeAll() } returns flowOf(emptyList())
        val repo = NoteRepositoryImpl(noteDao, tagDao)
        val r = repo.observeNotes(NoteFilter(tagId = 5L)).first()
        assertThat(r.map { it.id }).containsExactly(1L)
    }

    @Test
    fun `observeStats combines counts and tags`() = runTest {
        every { noteDao.countActive() } returns flowOf(3)
        every { noteDao.sumWordCount() } returns flowOf(120)
        every { tagDao.observeAll() } returns flowOf(listOf(
            TagEntity(id = 1, name = "x", color = 0, createdAt = 0),
            TagEntity(id = 2, name = "y", color = 0, createdAt = 0),
        ))
        every { noteDao.observeAllWithTags() } returns flowOf(listOf(
            NoteWithTags(NoteEntity(id = 1, title = "a", content = "", pinned = true, favorite = true, createdAt = 0, updatedAt = 0), emptyList()),
            NoteWithTags(NoteEntity(id = 2, title = "b", content = "", pinned = false, favorite = false, createdAt = 0, updatedAt = 0), emptyList()),
        ))
        val repo = NoteRepositoryImpl(noteDao, tagDao)
        val s = repo.observeStats().first()
        assertThat(s).isEqualTo(NoteStats(totalNotes = 3, totalWords = 120, totalTags = 2, pinnedCount = 1, favoriteCount = 1))
    }

    @Test
    fun `observeNotes search matches via dao`() = runTest {
        val hits = listOf(
            NoteEntity(id = 9, title = "Compose", content = "is fun", createdAt = 0, updatedAt = 0)
        )
        every { noteDao.search("Compose") } returns flowOf(hits)
        every { noteDao.observeAllWithTags() } returns flowOf(listOf(
            NoteWithTags(hits.first(), emptyList())
        ))
        every { tagDao.observeAll() } returns flowOf(emptyList())
        val repo = NoteRepositoryImpl(noteDao, tagDao)
        val r = repo.observeNotes(NoteFilter(query = "Compose")).first()
        assertThat(r.map { it.id }).containsExactly(9L)
    }

    @Test
    fun `removeTag delegates to dao`() = runTest {
        coJustRun { noteDao.deleteCross(any()) }
        val repo = NoteRepositoryImpl(noteDao, tagDao)
        repo.removeTag(noteId = 1L, tagId = 7L)
        coVerify(exactly = 1) { noteDao.deleteCross(any()) }
    }

    @Test
    fun `setReminder forwards`() = runTest {
        coJustRun { noteDao.setReminder(any(), any(), any()) }
        val repo = NoteRepositoryImpl(noteDao, tagDao)
        repo.setReminder(1L, 999L)
        coVerify(exactly = 1) { noteDao.setReminder(1L, 999L, any()) }
    }

    @Test
    fun `purgeOldTrash forwards`() = runTest {
        coJustRun { noteDao.purgeTrashBefore(any()) }
        val repo = NoteRepositoryImpl(noteDao, tagDao)
        repo.purgeOldTrash(1_000)
        coVerify(exactly = 1) { noteDao.purgeTrashBefore(any()) }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AiActionPromptTest {

    @Test fun `every action has unique title`() {
        val titles = AiAction.values().map { it.title }
        assertThat(titles).containsExactlyElementsIn(titles.toSet())
    }

    @Test fun `every action has non-blank prompt`() {
        AiAction.values().forEach { a ->
            assertThat(a.systemPrompt).isNotEmpty()
            assertThat(a.systemPrompt).contains("。")
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class StatsUseCaseTest {
    @Test
    fun `StatsUseCase returns repo flow`() = runTest {
        val repo = mockk<com.zhuji.note.domain.repository.NoteRepository>(relaxUnitFun = true)
        val s = NoteStats(1, 1, 1, 1, 1)
        every { repo.observeStats() } returns flowOf(s)
        val out = StatsUseCase(repo).invoke().first()
        assertThat(out).isEqualTo(s)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class GetNotesUseCaseTest {
    @Test
    fun `GetNotesUseCase respects filter`() = runTest {
        val repo = mockk<com.zhuji.note.domain.repository.NoteRepository>(relaxUnitFun = true)
        val list = listOf(Note(id = 1, title = "a", content = "b"))
        every { repo.observeNotes(any()) } returns flowOf(list)
        val r = GetNotesUseCase(repo).invoke(NoteFilter(query = "x")).first()
        assertThat(r).isEqualTo(list)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class PurgeUseCaseTest {
    @Test
    fun `PurgeTrashUseCase forwards`() = runTest {
        val repo = mockk<com.zhuji.note.domain.repository.NoteRepository>(relaxUnitFun = true)
        coJustRun { repo.purgeOldTrash(any()) }
        PurgeTrashUseCase(repo).invoke()
        coVerify { repo.purgeOldTrash(any()) }
    }
}
