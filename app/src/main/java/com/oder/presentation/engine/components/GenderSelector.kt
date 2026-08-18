package com.oder.presentation.engine.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oder.core.theme.DarkBorder
import com.oder.core.theme.DarkSurface
import com.oder.core.theme.NounFeminine
import com.oder.core.theme.NounMasculine
import com.oder.core.theme.NounNeuter
import com.oder.core.theme.OderTypography
import com.oder.core.theme.TextPrimary
import com.oder.core.theme.TextSecondary

data class GenderOption(
    val id: String,
    val label: String,
    val sublabel: String,
    val color: Color
)

@Composable
fun GenderSelector(
    selectedGender: String?,
    language: String = "de",
    onGenderSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val options = if (language == "de") {
        listOf(
            GenderOption("der", "der", "Maskulinum", NounMasculine),
            GenderOption("die", "die", "Femininum", NounFeminine),
            GenderOption("das", "das", "Neutrum", NounNeuter)
        )
    } else {
        listOf(
            GenderOption("ten", "ten", "Męski", NounMasculine),
            GenderOption("ta", "ta", "Żeński", NounFeminine),
            GenderOption("to", "to", "Nijaki", NounNeuter)
        )
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        options.forEach { option ->
            GenderButton(
                option = option,
                isSelected = selectedGender?.equals(option.id, ignoreCase = true) == true,
                onClick = { if (enabled) onGenderSelected(option.id) },
                enabled = enabled,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun GenderButton(
    option: GenderOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.94f else 1f,
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "gender_button_press"
    )

    val backgroundColor = if (isSelected) {
        option.color.copy(alpha = 0.25f)
    } else {
        DarkSurface
    }

    val borderColor = if (isSelected) {
        option.color
    } else {
        DarkBorder
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(vertical = 18.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = option.label,
                style = OderTypography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = if (isSelected) option.color else TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = option.sublabel,
                style = OderTypography.labelSmall,
                color = if (isSelected) option.color.copy(alpha = 0.85f) else TextSecondary
            )
        }
    }
}
