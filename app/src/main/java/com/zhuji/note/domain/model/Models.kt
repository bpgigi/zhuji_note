package com.zhuji.note.domain.model

data class Note(
    val id: Long = 0L,
    val title: String,
    val content: String,
    val color: Int = 0,
    val pinned: Boolean = false,
    val favorite: Boolean = false,
    val archived: Boolean = false,
    val deletedAt: Long? = null,
    val reminderAt: Long? = null,
    val tagIds: List<Long> = emptyList(),
    val folderId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val wordCount: Int = 0,
) {
    val isInTrash: Boolean get() = deletedAt != null
}

data class Tag(
    val id: Long = 0L,
    val name: String,
    val color: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
)

data class Folder(
    val id: Long = 0L,
    val name: String,
    val emoji: String = "📁",
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
)

enum class NoteOrder(val displayName: String) {
    UpdatedDesc("最近修改"),
    UpdatedAsc("最早修改"),
    CreatedDesc("最近创建"),
    Title("按标题"),
    WordCountDesc("字数最多"),
}

data class NoteFilter(
    val query: String = "",
    val folderId: Long? = null,
    val tagId: Long? = null,
    val onlyPinned: Boolean = false,
    val onlyFavorite: Boolean = false,
    val showArchived: Boolean = false,
    val showTrash: Boolean = false,
    val order: NoteOrder = NoteOrder.UpdatedDesc,
)
