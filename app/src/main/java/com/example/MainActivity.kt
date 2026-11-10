package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.AnimeScheduleScreen
import com.example.ui.AnimeViewModel
import com.example.ui.MainNavScreen
import com.example.ui.OnboardingScreen
import com.example.ui.SplashScreen
import com.example.ui.WatchlistScreen
import com.example.ui.theme.AniCalTheme

class MainActivity : ComponentActivity() {

    private val viewModel: AnimeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = getSharedPreferences("anical_prefs", Context.MODE_PRIVATE)

        setContent {
            val uiState = viewModel.uiState.collectAsState()
            var showSplash by remember { mutableStateOf(true) }
            var onboardingCompleted by remember {
                mutableStateOf(prefs.getBoolean("onboarding_completed", false))
            }

            AniCalTheme(themeMode = uiState.value.appThemeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    when {
                        showSplash -> {
                            SplashScreen(onSplashFinished = { showSplash = false })
                        }
                        !onboardingCompleted -> {
                            OnboardingScreen(onFinished = {
                                prefs.edit().putBoolean("onboarding_completed", true).apply()
                                onboardingCompleted = true
                            })
                        }
                        else -> {
                            when (uiState.value.currentNavScreen) {
                                MainNavScreen.SCHEDULE -> AnimeScheduleScreen(viewModel = viewModel)
                                MainNavScreen.WATCHLIST -> WatchlistScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
