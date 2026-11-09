package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.example.ui.AnimeScheduleScreen
import com.example.ui.AnimeViewModel
import com.example.ui.theme.AniCalTheme

class MainActivity : ComponentActivity() {

    private val viewModel: AnimeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState = viewModel.uiState.collectAsState()
            AniCalTheme(themeMode = uiState.value.appThemeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AnimeScheduleScreen(viewModel = viewModel)
                }
            }
        }
    }
}
