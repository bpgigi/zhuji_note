package com.zhuji.note.stage2

import com.google.common.truth.Truth.assertThat
import com.zhuji.note.domain.model.Note
import com.zhuji.note.domain.repository.NoteRepository
import com.zhuji.note.domain.usecase.DeleteNoteUseCase
import com.zhuji.note.domain.usecase.SaveNoteUseCase
import com.zhuji.note.domain.usecase.ToggleFlagsUseCase
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class UseCasesTest {

    @Test
    fun `SaveNoteUseCase rejects empty content and title`() = runTest {
        val repo = mockk<NoteRepository>(relaxUnitFun = true)
        val useCase = SaveNoteUseCase(repo)
        val res = useCase(Note(title = "", content = ""))
        assertThat(res.isFailure).isTrue()
    }

    @Test
    fun `SaveNoteUseCase delegates to repo upsert and returns id`() = runTest {
        val repo = mockk<NoteRepository>(relaxUnitFun = true)
        coEvery { repo.upsert(any()) } returns 11L
        val useCase = SaveNoteUseCase(repo)
        val res = useCase(Note(title = "t", content = "c"))
        assertThat(res.getOrNull()).isEqualTo(11L)
    }

    @Test
    fun `DeleteNoteUseCase soft delete delegates to repo`() = runTest {
        val repo = mockk<NoteRepository>(relaxUnitFun = true)
        coJustRun { repo.softDelete(any()) }
        val useCase = DeleteNoteUseCase(repo)
        useCase(7L, hard = false)
        coVerify(exactly = 1) { repo.softDelete(7L) }
    }

    @Test
    fun `DeleteNoteUseCase hard delete forwards to hardDelete`() = runTest {
        val repo = mockk<NoteRepository>(relaxUnitFun = true)
        coJustRun { repo.hardDelete(any()) }
        val useCase = DeleteNoteUseCase(repo)
        useCase(9L, hard = true)
        coVerify(exactly = 1) { repo.hardDelete(9L) }
    }

    @Test
    fun `ToggleFlagsUseCase pinned`() = runTest {
        val repo = mockk<NoteRepository>(relaxUnitFun = true)
        coJustRun { repo.setPinned(any(), any()) }
        ToggleFlagsUseCase(repo)(1L, ToggleFlagsUseCase.Flag.Pinned, true)
        coVerify(exactly = 1) { repo.setPinned(1L, true) }
    }

    @Test
    fun `ToggleFlagsUseCase favorite and archived`() = runTest {
        val repo = mockk<NoteRepository>(relaxUnitFun = true)
        coJustRun { repo.setFavorite(any(), any()) }
        coJustRun { repo.setArchived(any(), any()) }
        ToggleFlagsUseCase(repo)(2L, ToggleFlagsUseCase.Flag.Favorite, false)
        ToggleFlagsUseCase(repo)(3L, ToggleFlagsUseCase.Flag.Archived, true)
        coVerify(exactly = 1) { repo.setFavorite(2L, false) }
        coVerify(exactly = 1) { repo.setArchived(3L, true) }
    }
}
