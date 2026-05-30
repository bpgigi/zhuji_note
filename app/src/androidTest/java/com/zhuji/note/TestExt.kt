package com.zhuji.note

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText

fun ComposeTestRule.awaitText(text: String, substring: Boolean = false, timeoutMillis: Long = 20_000) {
    waitUntil(timeoutMillis) {
        onAllNodesWithText(text, substring = substring).fetchSemanticsNodes().isNotEmpty()
    }
}

fun ComposeTestRule.awaitContentDescription(desc: String, timeoutMillis: Long = 20_000) {
    waitUntil(timeoutMillis) {
        onAllNodesWithContentDescription(desc).fetchSemanticsNodes().isNotEmpty()
    }
}
