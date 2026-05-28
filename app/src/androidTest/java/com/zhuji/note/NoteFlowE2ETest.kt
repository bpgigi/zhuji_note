package com.zhuji.note

import androidx.compose.ui.test.assertIsDisplayed
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

    @Before fun setup() { hiltRule.inject() }

    @Test fun homeScreenShowsTitle() {
        composeRule.onNodeWithText("助记").assertIsDisplayed()
    }

    @Test fun fabNavigatesToNewNote() {
        composeRule.onNodeWithContentDescription("新笔记").performClick()
        composeRule.onNodeWithText("新笔记").assertIsDisplayed()
    }

    @Test fun createNoteAndSeeInList() {
        composeRule.onNodeWithContentDescription("新笔记").performClick()
        composeRule.onNode(hasText("标题")).performTextInput("E2E Test Note")
        composeRule.onNodeWithContentDescription("返回").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("E2E Test Note").assertIsDisplayed()
    }

    @Test fun settingsScreenOpens() {
        composeRule.onNodeWithContentDescription("设置").performClick()
        composeRule.onNodeWithText("API Key").assertIsDisplayed()
    }

    @Test fun statsScreenOpens() {
        composeRule.onNodeWithContentDescription("统计").performClick()
        composeRule.onNodeWithText("写作维度").assertIsDisplayed()
    }

    @Test fun trashScreenOpens() {
        composeRule.onNodeWithContentDescription("回收站").performClick()
        composeRule.onNodeWithText("回收站").assertIsDisplayed()
    }

    @Test fun aiChatScreenOpens() {
        composeRule.onNodeWithContentDescription("AI 助手").performClick()
        composeRule.onNodeWithText("AI 助手").assertIsDisplayed()
    }

    @Test fun darkModeToggleWorks() {
        composeRule.onNodeWithContentDescription("设置").performClick()
        composeRule.onNodeWithText("暗色模式").performClick()
        composeRule.waitForIdle()
    }

    @Test fun editNoteShowsMarkdownToolbar() {
        composeRule.onNodeWithContentDescription("新笔记").performClick()
        composeRule.onNodeWithText("H1").assertIsDisplayed()
        composeRule.onNodeWithText("H2").assertIsDisplayed()
    }

    @Test fun editNoteAiFabExists() {
        composeRule.onNodeWithContentDescription("新笔记").performClick()
        composeRule.onNodeWithContentDescription("AI 助手").assertIsDisplayed()
    }
}
