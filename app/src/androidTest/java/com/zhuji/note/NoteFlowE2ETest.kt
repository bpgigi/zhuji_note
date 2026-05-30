package com.zhuji.note

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NoteFlowE2ETest {
    @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)
    @get:Rule(order = 1) val composeRule = createAndroidComposeRule<MainActivity>()

    @Before fun setup() {
        hiltRule.inject()
        composeRule.awaitContentDescription("新笔记")
    }

    @Test fun homeScreenShowsTitle() {
        composeRule.awaitText("助记", substring = true)
        composeRule.onNodeWithText("助记", substring = true).assertExists()
    }

    @Test fun fabNavigatesToNewNote() {
        composeRule.onNodeWithContentDescription("新笔记").performClick()
        composeRule.awaitText("新笔记")
        composeRule.onNodeWithText("新笔记").assertExists()
    }

    @Test fun createNoteAndSeeInList() {
        composeRule.onNodeWithContentDescription("新笔记").performClick()
        composeRule.awaitText("标题")
        composeRule.onNode(hasText("标题")).performTextInput("E2E Test Note")
        composeRule.onNodeWithContentDescription("返回").performClick()
        composeRule.awaitText("E2E Test Note")
        composeRule.onNodeWithText("E2E Test Note").assertExists()
    }

    @Test fun settingsScreenOpens() {
        composeRule.onNodeWithContentDescription("设置").performClick()
        composeRule.awaitText("API Key")
        composeRule.onNodeWithText("API Key").assertExists()
    }

    @Test fun statsScreenOpens() {
        composeRule.onNodeWithContentDescription("统计").performClick()
        composeRule.awaitText("写作维度")
        composeRule.onNodeWithText("写作维度").assertExists()
    }

    @Test fun trashScreenOpens() {
        composeRule.onNodeWithContentDescription("回收站").performClick()
        composeRule.awaitText("回收站")
        composeRule.onNodeWithText("回收站").assertExists()
    }

    @Test fun aiChatScreenOpens() {
        composeRule.onNodeWithContentDescription("AI 助手").performClick()
        composeRule.awaitText("AI 助手")
        composeRule.onNodeWithText("AI 助手").assertExists()
    }

    @Test fun darkModeToggleWorks() {
        composeRule.onNodeWithContentDescription("设置").performClick()
        composeRule.awaitText("暗")
        composeRule.onNodeWithText("暗").performClick()
        composeRule.waitForIdle()
    }

    @Test fun editNoteShowsMarkdownToolbar() {
        composeRule.onNodeWithContentDescription("新笔记").performClick()
        composeRule.awaitText("H1")
        composeRule.onNodeWithText("H1").assertExists()
        composeRule.onNodeWithText("H2").assertExists()
    }

    @Test fun editNoteAiFabExists() {
        composeRule.onNodeWithContentDescription("新笔记").performClick()
        composeRule.awaitContentDescription("打开 AI 助手")
        composeRule.onNodeWithContentDescription("打开 AI 助手").assertExists()
    }
}
