package com.zhuji.note

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

    @Before fun setup() {
        hiltRule.inject()
        composeRule.awaitContentDescription("新笔记")
    }

    @Test fun navigateToSettingsAndBack() {
        composeRule.onNodeWithContentDescription("设置").performClick()
        composeRule.awaitText("API Key")
        composeRule.onNodeWithText("API Key").assertExists()
        composeRule.onNodeWithContentDescription("返回").performClick()
        composeRule.awaitContentDescription("新笔记")
        composeRule.onNodeWithContentDescription("新笔记").assertExists()
    }

    @Test fun navigateToStatsAndBack() {
        composeRule.onNodeWithContentDescription("统计").performClick()
        composeRule.awaitText("统计")
        composeRule.onNodeWithText("统计").assertExists()
        composeRule.onNodeWithContentDescription("返回").performClick()
    }

    @Test fun navigateToTrashAndBack() {
        composeRule.onNodeWithContentDescription("回收站").performClick()
        composeRule.awaitText("回收站")
        composeRule.onNodeWithText("回收站").assertExists()
        composeRule.onNodeWithContentDescription("返回").performClick()
    }

    @Test fun navigateToAiChatAndBack() {
        composeRule.onNodeWithContentDescription("AI 助手").performClick()
        composeRule.awaitText("AI 助手")
        composeRule.onNodeWithText("AI 助手").assertExists()
        composeRule.onNodeWithContentDescription("返回").performClick()
    }

    @Test fun navigateToNewNoteAndBack() {
        composeRule.onNodeWithContentDescription("新笔记").performClick()
        composeRule.awaitText("新笔记")
        composeRule.onNodeWithText("新笔记").assertExists()
        composeRule.onNodeWithContentDescription("返回").performClick()
    }

    @Test fun settingsShowsApiKeyField() {
        composeRule.onNodeWithContentDescription("设置").performClick()
        composeRule.awaitText("API Key")
        composeRule.onNodeWithText("API Key").assertExists()
    }

    @Test fun settingsShowsSaveButton() {
        composeRule.onNodeWithContentDescription("设置").performClick()
        composeRule.awaitText("保存并验证")
        composeRule.onNodeWithText("保存并验证").assertExists()
    }

    @Test fun trashShowsEmptyMessage() {
        composeRule.onNodeWithContentDescription("回收站").performClick()
        composeRule.awaitText("回收站为空", substring = true)
        composeRule.onNodeWithText("回收站为空", substring = true).assertExists()
    }

    @Test fun aiChatShowsWelcome() {
        composeRule.onNodeWithContentDescription("AI 助手").performClick()
        composeRule.awaitText("AI 助手")
        composeRule.onNodeWithText("AI 助手").assertExists()
    }

    @Test fun statsShowsWritingDimension() {
        composeRule.onNodeWithContentDescription("统计").performClick()
        composeRule.awaitText("写作维度")
        composeRule.onNodeWithText("写作维度").assertExists()
    }
}
