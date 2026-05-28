package com.zhuji.note.data.backup

import android.content.Context
import android.net.Uri
import com.zhuji.note.domain.model.Note
import com.zhuji.note.domain.repository.NoteRepository
import com.zhuji.note.domain.util.ExportFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class BackupExporter(
    private val context: Context,
    private val repo: NoteRepository,
) {

    suspend fun exportSingleMarkdown(note: Note, target: Uri) = withContext(Dispatchers.IO) {
        val md = ExportFormatter.toMarkdown(note.title, note.content, emptyList(), note.updatedAt)
        write(target) { it.write(md.toByteArray(Charsets.UTF_8)) }
    }

    suspend fun exportAsZip(target: Uri) = withContext(Dispatchers.IO) {
        val notes = repo.observeNotes(com.zhuji.note.domain.model.NoteFilter()).first()
        write(target) { stream ->
            ZipOutputStream(stream).use { zip ->
                notes.forEach { n ->
                    val safe = n.title.ifBlank { "untitled-${n.id}" }
                        .replace(Regex("[\\\\/:*?\"<>|]"), "_")
                    zip.putNextEntry(ZipEntry("$safe.md"))
                    zip.write(ExportFormatter.toMarkdown(n.title, n.content, emptyList(), n.updatedAt).toByteArray(Charsets.UTF_8))
                    zip.closeEntry()
                }
                zip.putNextEntry(ZipEntry("manifest.json"))
                zip.write(buildString {
                    append("{\"app\":\"zhujinote\",\"count\":${notes.size},\"exportedAt\":${System.currentTimeMillis()}}")
                }.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
        }
    }

    private inline fun write(target: Uri, block: (OutputStream) -> Unit) {
        context.contentResolver.openOutputStream(target)?.use { block(it) }
            ?: error("无法打开 $target")
    }
}

class BackupImporter(
    private val context: Context,
    private val repo: NoteRepository,
) {
    suspend fun importMarkdown(uri: Uri): Long = withContext(Dispatchers.IO) {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("无法读取 $uri")
        val text = String(bytes, Charsets.UTF_8)
        val firstLine = text.lineSequence().firstOrNull { it.isNotBlank() } ?: ""
        val title = firstLine.trimStart('#', ' ', '\t').take(40)
        val body = if (firstLine.startsWith("#")) text.substringAfter('\n', text) else text
        repo.upsert(Note(title = title, content = body))
    }
}
