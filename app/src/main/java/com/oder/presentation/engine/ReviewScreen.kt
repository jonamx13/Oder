package com.oder.presentation.engine

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.oder.core.theme.OderTypography
import com.oder.core.theme.OledBlack
import com.oder.core.theme.TextPrimary

@Composable
fun ReviewScreen(
    language: String = "de",
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OledBlack),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Review Engine ($language)",
            style = OderTypography.headlineMedium,
            color = TextPrimary
        )
    }
}
