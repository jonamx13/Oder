package com.oder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.oder.core.theme.OderTheme
import com.oder.core.theme.OledBlack
import com.oder.core.util.UserPreferencesRepository
import com.oder.presentation.navigation.OderNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OderTheme {
                val preferencesRepo = remember { UserPreferencesRepository(applicationContext) }
                val hasCompletedOnboarding by preferencesRepo.hasCompletedOnboarding.collectAsState(initial = null)

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(OledBlack)
                ) { innerPadding ->
                    val isComplete = hasCompletedOnboarding
                    if (isComplete != null) {
                        val navController = rememberNavController()
                        OderNavGraph(
                            navController = navController,
                            hasCompletedOnboarding = isComplete,
                            modifier = Modifier.padding(innerPadding)
                        )
                    } else {
                        // Clean dark surface while reading preferences
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(OledBlack)
                        )
                    }
                }
            }
        }
    }
}
