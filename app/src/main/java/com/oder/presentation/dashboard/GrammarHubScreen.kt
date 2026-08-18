package com.oder.presentation.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oder.core.theme.AccentSuccess
import com.oder.core.theme.DarkBorder
import com.oder.core.theme.DarkSurface
import com.oder.core.theme.DarkSurfaceElevated
import com.oder.core.theme.DarkSurfaceVariant
import com.oder.core.theme.NounMasculine
import com.oder.core.theme.OderTypography
import com.oder.core.theme.OledBlack
import com.oder.core.theme.TextPrimary
import com.oder.core.theme.TextSecondary
import com.oder.core.theme.TextTertiary

@Composable
fun GrammarHubScreen(
    modifier: Modifier = Modifier,
    initialLanguage: String = "de",
    viewModel: GrammarHubViewModel = viewModel(factory = GrammarHubViewModel.Factory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OledBlack)
    ) {
        // Top Header
        GrammarHubHeader(
            selectedLanguage = uiState.selectedLanguage,
            masteredCount = uiState.masteredCount,
            totalCount = uiState.totalCount,
            onSelectLanguage = { viewModel.selectLanguage(it) }
        )

        // Vertical Timeline List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            itemsIndexed(uiState.modules, key = { _, item -> item.module.id }) { index, item ->
                TimelineModuleRow(
                    item = item,
                    isLast = index == uiState.modules.lastIndex
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun GrammarHubHeader(
    selectedLanguage: String,
    masteredCount: Int,
    totalCount: Int,
    onSelectLanguage: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "LIBRARY",
                style = OderTypography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 3.sp,
                    fontSize = 22.sp
                ),
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$masteredCount of $totalCount Grammar Patterns Mastered",
                style = OderTypography.labelSmall.copy(letterSpacing = 0.5.sp),
                color = TextSecondary
            )
        }

        // Circular Flag Switcher
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularLanguageButton(
                flagEmoji = "🇩🇪",
                isSelected = selectedLanguage == "de",
                onClick = { onSelectLanguage("de") }
            )
            CircularLanguageButton(
                flagEmoji = "🇵🇱",
                isSelected = selectedLanguage == "pl",
                onClick = { onSelectLanguage("pl") }
            )
        }
    }
}

@Composable
private fun CircularLanguageButton(
    flagEmoji: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "flag_button_scale"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .size(44.dp)
            .clip(CircleShape)
            .background(if (isSelected) DarkSurfaceElevated else DarkSurface)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) NounMasculine else DarkBorder,
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = flagEmoji,
            fontSize = 18.sp
        )
    }
}

@Composable
private fun TimelineModuleRow(
    item: GrammarModuleWithRules,
    isLast: Boolean
) {
    val isMastered = item.module.isMastered
    var isExpanded by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "timeline_card_scale"
    )

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Vertical Timeline Column (Node + Line)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            // Node Icon Dot
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (isMastered) TextPrimary else DarkSurfaceElevated)
                    .border(
                        width = 1.dp,
                        color = if (isMastered) TextPrimary else DarkBorder,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isMastered) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Mastered",
                        tint = OledBlack,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = TextTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Connecting Vertical Line
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(110.dp)
                        .background(if (isMastered) DarkBorder else DarkSurfaceVariant)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Module Card
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 20.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(RoundedCornerShape(16.dp))
                .background(if (isMastered) DarkSurface else DarkSurface.copy(alpha = 0.5f))
                .border(
                    width = 1.dp,
                    color = if (isMastered) DarkBorder else DarkSurfaceVariant,
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { isExpanded = !isExpanded }
                )
                .padding(18.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.module.title,
                    style = OderTypography.titleMedium.copy(
                        fontWeight = if (isMastered) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isMastered) TextPrimary else TextTertiary,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isMastered) AccentSuccess.copy(alpha = 0.15f) else DarkSurfaceVariant)
                        .border(
                            1.dp,
                            if (isMastered) AccentSuccess.copy(alpha = 0.4f) else DarkBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isMastered) "UNLOCKED" else "LOCKED",
                        style = OderTypography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                        color = if (isMastered) AccentSuccess else TextTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Explanation Content
            Text(
                text = item.module.content,
                style = OderTypography.bodyMedium.copy(lineHeight = 22.sp),
                color = if (isMastered) TextSecondary else TextTertiary.copy(alpha = 0.7f),
                maxLines = if (isExpanded) Int.MAX_VALUE else 3
            )

            // Optional Expanded Rule Breakdown
            AnimatedVisibility(
                visible = isExpanded && item.rules.isNotEmpty(),
                enter = fadeIn(spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)),
                exit = fadeOut(spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Text(
                        text = "Sample Rule Triggers:",
                        style = OderTypography.labelSmall,
                        color = TextTertiary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    item.rules.forEach { rule ->
                        Text(
                            text = "• ${rule.errorMessage}",
                            style = OderTypography.bodySmall,
                            color = NounMasculine
                        )
                    }
                }
            }
        }
    }
}
