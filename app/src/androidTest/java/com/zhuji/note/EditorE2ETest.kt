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
class EditorE2ETest {
    @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)
    @get:Rule(order = 1) val composeRule = createAndroidComposeRule<MainActivity>()

    @Before fun setup() { hiltRule.inject() }

    @Test fun editorShowsToolbar() {
        composeRule.onNodeWithContentDescription("新笔记").performClick()
        composeRule.onNodeWithText("H1").assertIsDisplayed()
        composeRule.onNodeWithText("H2").assertIsDisplayed()
    }

    @Test fun editorShowsWordCount() {
        composeRule.onNodeWithContentDescription("新笔记").performClick()
        composeRule.onNodeWithText("0 字", substring = true).assertIsDisplayed()
    }

    @Test fun editorTitleInput() {
        composeRule.onNodeWithContentDescription("新笔记").performClick()
        composeRule.onNode(hasText("标题")).performTextInput("My Title")
        composeRule.onNodeWithText("My Title").assertIsDisplayed()
    }

    @Test fun editorContentInput() {
        composeRule.onNodeWithContentDescription("新笔记").performClick()
        composeRule.onNode(hasText("标题")).performTextInput("T")
        composeRule.onNode(hasText("写点什么吧", substring = true)).performTextInput("Hello World")
        composeRule.onNodeWithText("Hello World").assertIsDisplayed()
    }

    @Test fun editorBoldButton() {
        composeRule.onNodeWithContentDescription("新笔记").performClick()
        composeRule.onNodeWithText("加粗").performClick()
    }

    @Test fun editorItalicButton() {
        composeRule.onNodeWithContentDescription("新笔记").performClick()
        composeRule.onNodeWithText("斜体").performClick()
    }

    @Test fun editorCodeButton() {
        composeRule.onNodeWithContentDescription("新笔记").performClick()
        composeRule.onNodeWithText("代码").performClick()
    }

    @Test fun editorAiFabOpensSheet() {
        composeRule.onNodeWithContentDescription("新笔记").performClick()
        composeRule.onNode(hasText("标题")).performTextInput("Test")
        composeRule.onNode(hasText("写点什么吧", substring = true)).performTextInput("Some content for AI")
        composeRule.onNodeWithContentDescription("打开 AI 助手").performClick()
        composeRule.onNodeWithText("助记 AI", substring = true).assertIsDisplayed()
    }

    @Test fun editorAiSheetShowsActions() {
        composeRule.onNodeWithContentDescription("新笔记").performClick()
        composeRule.onNode(hasText("标题")).performTextInput("T")
        composeRule.onNode(hasText("写点什么吧", substring = true)).performTextInput("Content")
        composeRule.onNodeWithContentDescription("打开 AI 助手").performClick()
        composeRule.onNodeWithText("总结").assertIsDisplayed()
        composeRule.onNodeWithText("续写").assertIsDisplayed()
        composeRule.onNodeWithText("润色").assertIsDisplayed()
    }

    @Test fun editorBackNavigates() {
        composeRule.onNodeWithContentDescription("新笔记").performClick()
        composeRule.onNodeWithContentDescription("返回").performClick()
        composeRule.onNodeWithContentDescription("新笔记").assertIsDisplayed()
    }
}
