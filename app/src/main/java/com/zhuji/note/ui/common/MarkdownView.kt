package com.zhuji.note.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun MarkdownView(content: String, modifier: Modifier = Modifier) {
    MarkdownText(
        markdown = content,
        modifier = modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyLarge,
        linkColor = MaterialTheme.colorScheme.primary,
        isTextSelectable = true,
    )
}
