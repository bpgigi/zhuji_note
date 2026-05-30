package com.zhuji.note

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NavigationE2ETest {
    @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)
    @get:Rule(order = 1) val composeRule = createAndroidComposeRule<MainActivity>()

    @Before fun setup() { hiltRule.inject() }

    @Test fun navigateToSettingsAndBack() {
        composeRule.onNodeWithContentDescription("设置").performClick()
        composeRule.onNodeWithText("API Key").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("返回").performClick()
        composeRule.onNodeWithContentDescription("新笔记").assertIsDisplayed()
    }

    @Test fun navigateToStatsAndBack() {
        composeRule.onNodeWithContentDescription("统计").performClick()
        composeRule.onNodeWithText("统计").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("返回").performClick()
    }

    @Test fun navigateToTrashAndBack() {
        composeRule.onNodeWithContentDescription("回收站").performClick()
        composeRule.onNodeWithText("回收站").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("返回").performClick()
    }

    @Test fun navigateToAiChatAndBack() {
        composeRule.onNodeWithContentDescription("AI 助手").performClick()
        composeRule.onNodeWithText("AI 助手").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("返回").performClick()
    }

    @Test fun navigateToNewNoteAndBack() {
        composeRule.onNodeWithContentDescription("新笔记").performClick()
        composeRule.onNodeWithText("新笔记").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("返回").performClick()
    }

    @Test fun settingsShowsApiKeyField() {
        composeRule.onNodeWithContentDescription("设置").performClick()
        composeRule.onNodeWithText("API Key").assertIsDisplayed()
    }

    @Test fun settingsShowsSaveButton() {
        composeRule.onNodeWithContentDescription("设置").performClick()
        composeRule.onNodeWithText("保存并验证").assertIsDisplayed()
    }

    @Test fun trashShowsEmptyMessage() {
        composeRule.onNodeWithContentDescription("回收站").performClick()
        composeRule.onNodeWithText("回收站为空", substring = true).assertIsDisplayed()
    }

    @Test fun aiChatShowsWelcome() {
        composeRule.onNodeWithContentDescription("AI 助手").performClick()
        composeRule.onNodeWithText("AI 助手").assertIsDisplayed()
    }

    @Test fun statsShowsWritingDimension() {
        composeRule.onNodeWithContentDescription("统计").performClick()
        composeRule.onNodeWithText("写作维度").assertIsDisplayed()
    }
}
