package com.zhuji.note

import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zhuji.note.data.local.preferences.ThemeMode
import com.zhuji.note.ui.theme.ZhujiTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThemeRendersTest {

    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test fun lightTheme_rendersText() {
        composeRule.setContent {
            ZhujiTheme(themeMode = ThemeMode.Light) {
                Surface { Text("hello-light") }
            }
        }
        composeRule.onNodeWithText("hello-light").assertIsDisplayed()
    }

    @Test fun darkTheme_rendersText() {
        composeRule.setContent {
            ZhujiTheme(themeMode = ThemeMode.Dark) {
                Surface { Text("hello-dark") }
            }
        }
        composeRule.onNodeWithText("hello-dark").assertIsDisplayed()
    }
}
