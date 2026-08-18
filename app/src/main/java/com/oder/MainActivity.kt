package com.oder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.oder.core.theme.OderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    OderRoot(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun OderRoot(modifier: Modifier = Modifier) {
    Text(
        text = "Oder Engine Initialized",
        modifier = modifier
    )
}
