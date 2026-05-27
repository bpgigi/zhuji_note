package com.zhuji.note

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zhuji.note.ui.common.GradientText
import com.zhuji.note.ui.common.TypingDot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CommonComposeTest {

    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test fun gradientText_renders() {
        rule.setContent { GradientText(text = "助记 Zhuji") }
        rule.onNodeWithText("助记 Zhuji").assertIsDisplayed()
    }

    @Test fun typingDot_compositionDoesNotCrash() {
        rule.setContent { TypingDot() }
        // No assertion needed: lack of exception is the success criterion
    }

    @Test fun composableSetContent_supportsPlainText() {
        rule.setContent { Text("plain", modifier = androidx.compose.ui.Modifier) }
        rule.onNodeWithText("plain").assertIsDisplayed()
    }
}
