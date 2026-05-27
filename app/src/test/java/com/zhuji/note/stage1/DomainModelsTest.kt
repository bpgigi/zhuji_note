package com.zhuji.note.stage1

import com.google.common.truth.Truth.assertThat
import com.zhuji.note.domain.model.Folder
import com.zhuji.note.domain.model.Note
import com.zhuji.note.domain.model.NoteFilter
import com.zhuji.note.domain.model.NoteOrder
import com.zhuji.note.domain.model.Tag
import org.junit.Test

class DomainModelsTest {

    @Test fun `note default values`() {
        val n = Note(title = "t", content = "c")
        assertThat(n.id).isEqualTo(0L)
        assertThat(n.pinned).isFalse()
        assertThat(n.favorite).isFalse()
        assertThat(n.archived).isFalse()
        assertThat(n.deletedAt).isNull()
        assertThat(n.isInTrash).isFalse()
    }

    @Test fun `note in trash flag`() {
        val n = Note(title = "t", content = "c", deletedAt = 1L)
        assertThat(n.isInTrash).isTrue()
    }

    @Test fun `note copy preserves immutability`() {
        val n = Note(title = "a", content = "c")
        val n2 = n.copy(title = "b")
        assertThat(n.title).isEqualTo("a")
        assertThat(n2.title).isEqualTo("b")
    }

    @Test fun `tag construct`() {
        val t = Tag(name = "想法", color = 0xFF112233.toInt())
        assertThat(t.name).isEqualTo("想法")
        assertThat(t.color).isEqualTo(0xFF112233.toInt())
    }

    @Test fun `folder default emoji`() {
        val f = Folder(name = "工作")
        assertThat(f.emoji).isEqualTo("📁")
    }

    @Test fun `default filter`() {
        val f = NoteFilter()
        assertThat(f.query).isEmpty()
        assertThat(f.order).isEqualTo(NoteOrder.UpdatedDesc)
    }

    @Test fun `note order display name unique`() {
        val names = NoteOrder.values().map { it.displayName }
        assertThat(names).containsExactlyElementsIn(names.toSet())
    }
}
