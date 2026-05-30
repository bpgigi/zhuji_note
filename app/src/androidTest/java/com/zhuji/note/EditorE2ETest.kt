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
class EditorE2ETest {
    @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)
    @get:Rule(order = 1) val composeRule = createAndroidComposeRule<MainActivity>()

    @Before fun setup() {
        hiltRule.inject()
        composeRule.awaitContentDescription("新笔记")
    }

    private fun openEditor() {
        composeRule.onNodeWithContentDescription("新笔记").performClick()
        composeRule.awaitText("标题")
    }

    @Test fun editorShowsToolbar() {
        openEditor()
        composeRule.awaitText("H1")
        composeRule.onNodeWithText("H1").assertExists()
        composeRule.onNodeWithText("H2").assertExists()
    }

    @Test fun editorShowsWordCount() {
        openEditor()
        composeRule.awaitText("字", substring = true)
        composeRule.onNodeWithText("字", substring = true).assertExists()
    }

    @Test fun editorTitleInput() {
        openEditor()
        composeRule.onNode(hasText("标题")).performTextInput("My Title")
        composeRule.awaitText("My Title")
        composeRule.onNodeWithText("My Title").assertExists()
    }

    @Test fun editorContentInput() {
        openEditor()
        composeRule.onNode(hasText("标题")).performTextInput("T")
        composeRule.onNode(hasText("写点什么吧", substring = true)).performTextInput("Hello World")
        composeRule.awaitText("Hello World")
        composeRule.onNodeWithText("Hello World").assertExists()
    }

    @Test fun editorBoldButton() {
        openEditor()
        composeRule.awaitText("加粗")
        composeRule.onNodeWithText("加粗").performClick()
    }

    @Test fun editorItalicButton() {
        openEditor()
        composeRule.awaitText("斜体")
        composeRule.onNodeWithText("斜体").performClick()
    }

    @Test fun editorCodeButton() {
        openEditor()
        composeRule.awaitText("代码")
        composeRule.onNodeWithText("代码").performClick()
    }

    @Test fun editorAiFabOpensSheet() {
        openEditor()
        composeRule.onNode(hasText("标题")).performTextInput("Test")
        composeRule.onNode(hasText("写点什么吧", substring = true)).performTextInput("Some content for AI")
        composeRule.onNodeWithContentDescription("打开 AI 助手").performClick()
        composeRule.awaitText("助记 AI", substring = true)
        composeRule.onNodeWithText("助记 AI", substring = true).assertExists()
    }

    @Test fun editorAiSheetShowsActions() {
        openEditor()
        composeRule.onNode(hasText("标题")).performTextInput("T")
        composeRule.onNode(hasText("写点什么吧", substring = true)).performTextInput("Content")
        composeRule.onNodeWithContentDescription("打开 AI 助手").performClick()
        composeRule.awaitText("总结")
        composeRule.onNodeWithText("总结").assertExists()
        composeRule.onNodeWithText("续写").assertExists()
        composeRule.onNodeWithText("润色").assertExists()
    }

    @Test fun editorBackNavigates() {
        composeRule.onNodeWithContentDescription("新笔记").performClick()
        composeRule.awaitContentDescription("返回")
        composeRule.onNodeWithContentDescription("返回").performClick()
        composeRule.awaitContentDescription("新笔记")
        composeRule.onNodeWithContentDescription("新笔记").assertExists()
    }
}
