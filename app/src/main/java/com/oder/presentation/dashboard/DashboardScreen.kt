package com.oder.presentation.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.oder.core.theme.NounFeminine
import com.oder.core.theme.NounMasculine
import com.oder.core.theme.NounNeuter
import com.oder.core.theme.OderTypography
import com.oder.core.theme.OledBlack
import com.oder.core.theme.TextPrimary
import com.oder.core.theme.TextSecondary
import com.oder.core.theme.TextTertiary

enum class DashboardTab {
    TRAINING,
    LIBRARY
}

@Composable
fun DashboardScreen(
    onNavigateToReview: (language: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentTab by remember { mutableStateOf(DashboardTab.TRAINING) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OledBlack)
    ) {
        // Main Tab Content Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn(
                        animationSpec = spring(
                            stiffness = Spring.StiffnessLow,
                            dampingRatio = Spring.DampingRatioMediumBouncy
                        )
                    ) togetherWith fadeOut(
                        animationSpec = spring(
                            stiffness = Spring.StiffnessLow,
                            dampingRatio = Spring.DampingRatioMediumBouncy
                        )
                    )
                },
                label = "dashboard_tab_crossfade"
            ) { tab ->
                when (tab) {
                    DashboardTab.TRAINING -> TrainingTabView(
                        uiState = uiState,
                        onSelectLanguage = { viewModel.selectLanguage(it) },
                        onNavigateToReview = onNavigateToReview
                    )
                    DashboardTab.LIBRARY -> GrammarHubScreen(
                        initialLanguage = uiState.selectedLanguage
                    )
                }
            }
        }

        // Minimalist Bottom Navigation Bar
        MinimalistBottomNavBar(
            currentTab = currentTab,
            onSelectTab = { currentTab = it }
        )
    }
}

@Composable
private fun TrainingTabView(
    uiState: DashboardUiState,
    onSelectLanguage: (String) -> Unit,
    onNavigateToReview: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OledBlack)
    ) {
        // Custom Flag Top Bar
        CustomLanguageTopBar(
            selectedLanguage = uiState.selectedLanguage,
            onSelectLanguage = onSelectLanguage
        )

        // Animated Body
        AnimatedContent(
            targetState = uiState.selectedLanguage,
            transitionSpec = {
                fadeIn(
                    animationSpec = spring(
                        stiffness = Spring.StiffnessLow,
                        dampingRatio = Spring.DampingRatioMediumBouncy
                    )
                ) togetherWith fadeOut(
                    animationSpec = spring(
                        stiffness = Spring.StiffnessLow,
                        dampingRatio = Spring.DampingRatioMediumBouncy
                    )
                )
            },
            label = "language_content_crossfade",
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) { language ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                LanguageHeaderSection(
                    language = language,
                    totalVocab = uiState.totalVocabulary
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Massive Centered Button
                StartDailySessionButton(
                    dueCardsCount = uiState.dueCardsCount,
                    onClick = { onNavigateToReview(language) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Grammar Mastery Section
                GrammarMasterySection(
                    nounMastery = uiState.nounMastery,
                    verbMastery = uiState.verbMastery,
                    caseMastery = uiState.caseMastery,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun MinimalistBottomNavBar(
    currentTab: DashboardTab,
    onSelectTab: (DashboardTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .border(width = 1.dp, color = DarkBorder)
            .padding(vertical = 12.dp, horizontal = 32.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavTabItem(
            label = "Training",
            icon = Icons.Default.Home,
            isSelected = currentTab == DashboardTab.TRAINING,
            onClick = { onSelectTab(DashboardTab.TRAINING) }
        )

        BottomNavTabItem(
            label = "Library",
            icon = Icons.AutoMirrored.Filled.MenuBook,
            isSelected = currentTab == DashboardTab.LIBRARY,
            onClick = { onSelectTab(DashboardTab.LIBRARY) }
        )
    }
}

@Composable
private fun BottomNavTabItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "nav_tab_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) TextPrimary else TextTertiary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = OderTypography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 11.sp
            ),
            color = if (isSelected) TextPrimary else TextTertiary
        )
    }
}

@Composable
private fun CustomLanguageTopBar(
    selectedLanguage: String,
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
                text = "ODER",
                style = OderTypography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 3.sp,
                    fontSize = 22.sp
                ),
                color = TextPrimary
            )
            Text(
                text = "Language Engine",
                style = OderTypography.labelSmall.copy(letterSpacing = 1.sp),
                color = TextTertiary
            )
        }

        // Circular Flag Icons Switcher
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularFlagButton(
                flagEmoji = "🇩🇪",
                languageCode = "de",
                label = "German",
                isSelected = selectedLanguage == "de",
                onClick = { onSelectLanguage("de") }
            )
            CircularFlagButton(
                flagEmoji = "🇵🇱",
                languageCode = "pl",
                label = "Polish",
                isSelected = selectedLanguage == "pl",
                onClick = { onSelectLanguage("pl") }
            )
        }
    }
}

@Composable
private fun CircularFlagButton(
    flagEmoji: String,
    languageCode: String,
    label: String,
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
            .size(46.dp)
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
            fontSize = 20.sp
        )
    }
}

@Composable
private fun LanguageHeaderSection(
    language: String,
    totalVocab: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        val title = if (language == "de") "Deutsch B2" else "Polski B2"
        val subtitle = if (language == "de") "Kasus, Rektion & Präpositionen" else "Aspekt, Deklinacja & Przypadki"

        Text(
            text = title,
            style = OderTypography.displayMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = OderTypography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
private fun StartDailySessionButton(
    dueCardsCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEnabled = dueCardsCount > 0
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && isEnabled) 0.96f else 1f,
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "main_session_btn_scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(20.dp))
            .background(if (isEnabled) TextPrimary else DarkSurfaceElevated)
            .border(
                width = 1.dp,
                color = if (isEnabled) TextPrimary else DarkBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = isEnabled,
                onClick = onClick
            )
            .padding(vertical = 32.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isEnabled) "Start Daily Session" else "All Caught Up",
                    style = OderTypography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = if (isEnabled) OledBlack else TextSecondary
                )

                if (isEnabled) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = OledBlack,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = AccentSuccess,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isEnabled) "$dueCardsCount words ready for review" else "No reviews due right now",
                style = OderTypography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = if (isEnabled) Color(0xFF404040) else TextTertiary
            )
        }
    }
}

@Composable
private fun GrammarMasterySection(
    nounMastery: GrammarCategoryMastery,
    verbMastery: GrammarCategoryMastery,
    caseMastery: GrammarCategoryMastery,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Grammar Mastery",
                style = OderTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Text(
                text = "Live Progress",
                style = OderTypography.labelSmall,
                color = TextTertiary
            )
        }

        // Sleek Linear Progress Rows (0 - 100%)
        GrammarProgressRow(
            title = nounMastery.categoryName,
            description = nounMastery.description,
            progress = nounMastery.progress,
            barColor = NounMasculine
        )

        GrammarProgressRow(
            title = verbMastery.categoryName,
            description = verbMastery.description,
            progress = verbMastery.progress,
            barColor = NounNeuter
        )

        GrammarProgressRow(
            title = caseMastery.categoryName,
            description = caseMastery.description,
            progress = caseMastery.progress,
            barColor = NounFeminine
        )
    }
}

@Composable
private fun GrammarProgressRow(
    title: String,
    description: String,
    progress: Float,
    barColor: Color
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "grammar_progress_anim"
    )

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = title,
                    style = OderTypography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
                Text(
                    text = description,
                    style = OderTypography.bodySmall,
                    color = TextTertiary
                )
            }

            Text(
                text = "${(progress * 100).toInt()}%",
                style = OderTypography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = barColor,
            trackColor = DarkSurfaceVariant
        )
    }
}
