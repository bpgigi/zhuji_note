package com.zhuji.note.stage3

import com.google.common.truth.Truth.assertThat
import com.zhuji.note.ui.theme.ClaudeCanvas
import com.zhuji.note.ui.theme.ClaudeCoral
import com.zhuji.note.ui.theme.DarkColors
import com.zhuji.note.ui.theme.LightColors
import com.zhuji.note.ui.theme.WindsurfCharcoal
import com.zhuji.note.ui.theme.WindsurfNeon
import com.zhuji.note.ui.theme.ZhujiCornerTokens
import com.zhuji.note.ui.theme.ZhujiTypography
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ThemeAndComposeTest {

    @Test fun `light scheme uses claude tokens`() {
        assertThat(LightColors.primary).isEqualTo(ClaudeCoral)
        assertThat(LightColors.background).isEqualTo(ClaudeCanvas)
    }

    @Test fun `dark scheme uses windsurf tokens`() {
        assertThat(DarkColors.primary).isEqualTo(WindsurfNeon)
        assertThat(DarkColors.background).isEqualTo(WindsurfCharcoal)
    }

    @Test fun `typography styles configured`() {
        listOf(
            ZhujiTypography.displayLarge, ZhujiTypography.displayMedium, ZhujiTypography.displaySmall,
            ZhujiTypography.headlineLarge, ZhujiTypography.headlineMedium, ZhujiTypography.headlineSmall,
            ZhujiTypography.titleLarge, ZhujiTypography.titleMedium, ZhujiTypography.titleSmall,
            ZhujiTypography.bodyLarge, ZhujiTypography.bodyMedium, ZhujiTypography.bodySmall,
            ZhujiTypography.labelLarge, ZhujiTypography.labelMedium, ZhujiTypography.labelSmall,
        ).forEach { style ->
            assertThat(style.fontSize.value).isGreaterThan(0f)
            assertThat(style.lineHeight.value).isGreaterThan(0f)
        }
    }

    @Test fun `corner tokens defined`() {
        assertThat(ZhujiCornerTokens.NoteCard.toString()).isNotEmpty()
        assertThat(ZhujiCornerTokens.FAB.toString()).isNotEmpty()
        assertThat(ZhujiCornerTokens.Sheet.toString()).isNotEmpty()
    }
}
