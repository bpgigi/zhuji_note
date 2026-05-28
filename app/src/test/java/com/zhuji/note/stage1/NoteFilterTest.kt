package com.zhuji.note.stage1

import com.google.common.truth.Truth.assertThat
import com.zhuji.note.domain.model.NoteFilter
import com.zhuji.note.domain.model.NoteOrder
import org.junit.Test

class NoteFilterTest {
    @Test fun `default filter has no search`() {
        val f = NoteFilter()
        assertThat(f.query).isEmpty()
        assertThat(f.onlyFavorite).isFalse()
        assertThat(f.onlyPinned).isFalse()
    }
    @Test fun `filter with search`() {
        val f = NoteFilter(query = "hello")
        assertThat(f.query).isEqualTo("hello")
    }
    @Test fun `filter with tag`() {
        val f = NoteFilter(tagId = 5L)
        assertThat(f.tagId).isEqualTo(5L)
    }
    @Test fun `all orders exist`() {
        assertThat(NoteOrder.entries).hasSize(NoteOrder.entries.size)
        assertThat(NoteOrder.entries.map { it.name }).contains("UpdatedDesc")
        assertThat(NoteOrder.entries.map { it.name }).contains("Title")
    }
    @Test fun `filter copy works`() {
        val f = NoteFilter(onlyFavorite = true)
        val f2 = f.copy(onlyPinned = true)
        assertThat(f2.onlyFavorite).isTrue()
        assertThat(f2.onlyPinned).isTrue()
    }
}

