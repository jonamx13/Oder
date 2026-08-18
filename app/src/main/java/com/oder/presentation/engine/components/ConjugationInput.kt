package com.oder.presentation.engine.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oder.core.theme.AccentError
import com.oder.core.theme.DarkBorder
import com.oder.core.theme.DarkSurface
import com.oder.core.theme.NounMasculine
import com.oder.core.theme.OderTypography
import com.oder.core.theme.OledBlack
import com.oder.core.theme.TextPrimary
import com.oder.core.theme.TextTertiary

@Composable
fun ConjugationInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Type inflection or target form...",
    enabled: Boolean = true,
    isError: Boolean = false
) {
    val submitInteractionSource = remember { MutableInteractionSource() }
    val isSubmitPressed by submitInteractionSource.collectIsPressedAsState()

    val submitScale by animateFloatAsState(
        targetValue = if (isSubmitPressed && enabled) 0.92f else 1f,
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "submit_icon_scale"
    )

    val borderColor = when {
        isError -> AccentError
        value.isNotBlank() -> NounMasculine
        else -> DarkBorder
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = OderTypography.bodyLarge,
                    color = TextTertiary
                )
            }

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                textStyle = OderTypography.bodyLarge.copy(
                    color = TextPrimary,
                    fontSize = 18.sp
                ),
                cursorBrush = SolidColor(TextPrimary),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    autoCorrectEnabled = false
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (value.isNotBlank() && enabled) {
                            onSubmit()
                        }
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Tactile Submit Arrow Button
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = submitScale
                    scaleY = submitScale
                }
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (value.isNotBlank() && enabled) TextPrimary else DarkBorder)
                .clickable(
                    interactionSource = submitInteractionSource,
                    indication = null,
                    enabled = enabled && value.isNotBlank(),
                    onClick = onSubmit
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Submit Answer",
                tint = if (value.isNotBlank() && enabled) OledBlack else TextTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
