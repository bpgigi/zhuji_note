package com.zhuji.note.stage3

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.zhuji.note.data.local.preferences.ThemeMode
import com.zhuji.note.data.local.preferences.UserPreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class UserPreferencesDataStoreTest {

    private fun newStore() = UserPreferencesDataStore(ApplicationProvider.getApplicationContext())

    @Test fun `default model is flash`() = runTest {
        val s = newStore().flow.first()
        assertThat(s.deepseekModel).isNotEmpty()
        assertThat(s.deepseekModel).contains("deepseek")
    }

    @Test fun `setTheme persists`() = runTest {
        val s = newStore()
        s.setTheme(ThemeMode.Dark)
        assertThat(s.flow.first().theme).isEqualTo(ThemeMode.Dark)
    }

    @Test fun `setKey trims whitespace`() = runTest {
        val s = newStore()
        s.setKey("  sk-foo  ")
        assertThat(s.flow.first().deepseekKey).isEqualTo("sk-foo")
    }

    @Test fun `setModel persists`() = runTest {
        val s = newStore()
        s.setModel("deepseek-v4-pro")
        assertThat(s.flow.first().deepseekModel).isEqualTo("deepseek-v4-pro")
    }

    @Test fun `setDynamic toggles`() = runTest {
        val s = newStore()
        s.setDynamic(true)
        assertThat(s.flow.first().dynamicColor).isTrue()
        s.setDynamic(false)
        assertThat(s.flow.first().dynamicColor).isFalse()
    }

    @Test fun `setNeon toggles`() = runTest {
        val s = newStore()
        s.setNeon(true)
        assertThat(s.flow.first().accentNeon).isTrue()
    }

    @Test fun `setFontScale persists`() = runTest {
        val s = newStore()
        s.setFontScale(2)
        assertThat(s.fontScale.first()).isEqualTo(2)
    }

    @Test fun `consumeFirstRun toggles flag`() = runTest {
        val s = newStore()
        s.consumeFirstRun()
        assertThat(s.flow.first().firstRun).isFalse()
    }
}
