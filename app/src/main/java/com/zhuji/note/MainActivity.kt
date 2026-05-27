package com.zhuji.note

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhuji.note.data.local.preferences.UserPreferencesDataStore
import com.zhuji.note.ui.ZhujiApp
import com.zhuji.note.ui.theme.ZhujiTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var prefs: UserPreferencesDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val state by prefs.flow.collectAsStateWithLifecycle(initialValue = com.zhuji.note.data.local.preferences.UserPrefs())
            ZhujiTheme(themeMode = state.theme, dynamicColor = state.dynamicColor) {
                ZhujiApp()
            }
        }
    }
}
