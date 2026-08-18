package com.oder.presentation.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oder.core.theme.AccentSuccess
import com.oder.core.theme.DarkBorder
import com.oder.core.theme.DarkSurface
import com.oder.core.theme.DarkSurfaceElevated
import com.oder.core.theme.DarkSurfaceVariant
import com.oder.core.theme.NounMasculine
import com.oder.core.theme.NounNeuter
import com.oder.core.theme.OderTypography
import com.oder.core.theme.OledBlack
import com.oder.core.theme.TextPrimary
import com.oder.core.theme.TextSecondary
import com.oder.core.theme.TextTertiary

@Composable
fun DashboardScreen(
    onNavigateToReview: (language: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OledBlack)
    ) {
        // --- Top Bar with AnimatedContent Language Switcher ---
        DashboardTopBar(
            selectedLanguage = uiState.selectedLanguage,
            onSelectLanguage = { viewModel.selectLanguage(it) }
        )

        // --- Main Content Grid ---
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 320.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Status Section
            item(span = { GridItemSpan(maxLineSpan) }) {
                HeroStatusCard(
                    uiState = uiState,
                    onStartReview = { onNavigateToReview(uiState.selectedLanguage.code) }
                )
            }

            // Grammar Matrices Header
            item(span = { GridItemSpan(maxLineSpan) }) {
                GrammarMatricesHeader()
            }

            // Grammar Matrix Cards
            items(uiState.matrices, key = { it.id }) { matrixItem ->
                GrammarMatrixCard(matrixItem = matrixItem)
            }

            // Bottom Spacing
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun DashboardTopBar(
    selectedLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "ODER",
                style = OderTypography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = TextPrimary
            )
            Text(
                text = "Language Mastery",
                style = OderTypography.labelSmall,
                color = TextTertiary
            )
        }

        // Language Switcher Pill
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(DarkSurfaceVariant)
                .border(1.dp, DarkBorder, RoundedCornerShape(24.dp))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LanguageTabPill(
                language = AppLanguage.GERMAN,
                isSelected = selectedLanguage == AppLanguage.GERMAN,
                onClick = { onSelectLanguage(AppLanguage.GERMAN) }
            )
            Spacer(modifier = Modifier.width(4.dp))
            LanguageTabPill(
                language = AppLanguage.POLISH,
                isSelected = selectedLanguage == AppLanguage.POLISH,
                onClick = { onSelectLanguage(AppLanguage.POLISH) }
            )
        }
    }
}

@Composable
private fun LanguageTabPill(
    language: AppLanguage,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "language_pill_press"
    )

    val backgroundAnim = if (isSelected) DarkSurfaceElevated else Color.Transparent
    val borderModifier = if (isSelected) Modifier.border(1.dp, DarkBorder, RoundedCornerShape(20.dp)) else Modifier

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundAnim)
            .then(borderModifier)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = isSelected,
            transitionSpec = {
                (fadeIn(spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)) +
                    scaleIn(initialScale = 0.9f, animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)))
                    .togetherWith(
                        fadeOut(spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)) +
                            scaleOut(targetScale = 0.9f, animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy))
                    )
            },
            label = "tab_label_anim"
        ) { selected ->
            Text(
                text = language.badge,
                style = OderTypography.labelLarge.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (selected) TextPrimary else TextTertiary
            )
        }
    }
}

@Composable
private fun HeroStatusCard(
    uiState: DashboardUiState,
    onStartReview: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        AnimatedContent(
            targetState = uiState.selectedLanguage,
            transitionSpec = {
                fadeIn(spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy))
                    .togetherWith(fadeOut(spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)))
            },
            label = "hero_language_text"
        ) { language ->
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(AccentSuccess)
                    )
                    Text(
                        text = language.displayName.uppercase(),
                        style = OderTypography.labelMedium.copy(letterSpacing = 1.5.sp),
                        color = NounMasculine
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = language.subtitle,
                    style = OderTypography.headlineMedium,
                    color = TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Practice status metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusMetricBox(
                label = "Ready for Review",
                value = "${uiState.dueCardsCount}",
                modifier = Modifier.weight(1f),
                accentColor = TextPrimary
            )
            StatusMetricBox(
                label = "Total Vocabulary",
                value = "${uiState.totalLexemes}",
                modifier = Modifier.weight(1f),
                accentColor = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Large Tactile CTA Button
        StartTrainingButton(
            dueCount = uiState.dueCardsCount,
            onClick = onStartReview
        )
    }
}

@Composable
private fun StatusMetricBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accentColor: Color = TextPrimary
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Text(
            text = value,
            style = OderTypography.displaySmall.copy(fontWeight = FontWeight.Bold),
            color = accentColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = OderTypography.bodySmall,
            color = TextTertiary
        )
    }
}

@Composable
private fun StartTrainingButton(
    dueCount: Int,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "cta_press_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(12.dp))
            .background(TextPrimary)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 16.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Start Daily Practice",
                style = OderTypography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = OledBlack
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(OledBlack)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$dueCount",
                    style = OderTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun GrammarMatricesHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Text(
            text = "Grammar Topics",
            style = OderTypography.titleLarge,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Practice key sentence structures and inflection patterns",
            style = OderTypography.bodySmall,
            color = TextTertiary
        )
    }
}

@Composable
private fun GrammarMatrixCard(
    matrixItem: GrammarMatrixItem
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "matrix_card_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {}
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = matrixItem.title,
                style = OderTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${(matrixItem.masteryRate * 100).toInt()}%",
                style = OderTypography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = if (matrixItem.masteryRate >= 0.7f) NounNeuter else TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = matrixItem.caseOrTopic,
            style = OderTypography.bodySmall,
            color = TextSecondary,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(14.dp))

        LinearProgressIndicator(
            progress = { matrixItem.masteryRate },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (matrixItem.masteryRate >= 0.7f) NounNeuter else NounMasculine,
            trackColor = DarkSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${matrixItem.activeRules} of ${matrixItem.totalRules} rules learned",
                style = OderTypography.labelSmall,
                color = TextTertiary
            )
            Text(
                text = if (matrixItem.masteryRate >= 0.75f) "Mastered" else "In Progress",
                style = OderTypography.labelSmall,
                color = if (matrixItem.masteryRate >= 0.75f) AccentSuccess else TextTertiary
            )
        }
    }
}
