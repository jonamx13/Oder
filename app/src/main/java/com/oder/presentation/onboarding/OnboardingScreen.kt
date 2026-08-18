package com.oder.presentation.onboarding

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import com.oder.core.theme.NounMasculine
import com.oder.core.theme.NounNeuter
import com.oder.core.theme.OderTypography
import com.oder.core.theme.OledBlack
import com.oder.core.theme.TextPrimary
import com.oder.core.theme.TextSecondary
import com.oder.core.theme.TextTertiary

@Composable
fun OnboardingScreen(
    onNavigateToDashboard: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OledBlack)
    ) {
        AnimatedContent(
            targetState = uiState.step,
            transitionSpec = {
                (fadeIn(spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)) +
                    scaleIn(initialScale = 0.95f, animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)))
                    .togetherWith(
                        fadeOut(spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)) +
                            scaleOut(targetScale = 0.95f, animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy))
                    )
            },
            label = "onboarding_step_transition"
        ) { step ->
            when (step) {
                OnboardingStep.WELCOME -> WelcomeStepView(
                    selectedLanguage = uiState.selectedLanguage,
                    onSelectLanguage = { viewModel.selectLanguage(it) },
                    onStartPlacement = { viewModel.startPlacementTest() },
                    onStartScratch = { viewModel.startFromScratch(context) }
                )

                OnboardingStep.DIAGNOSTIC -> DiagnosticStepView(
                    uiState = uiState,
                    questions = viewModel.getQuestions(uiState.selectedLanguage),
                    onSelectOption = { viewModel.selectOption(it) },
                    onSubmitAnswer = { viewModel.submitAnswer(context) }
                )

                OnboardingStep.CALIBRATION_COMPLETE -> CalibrationCompleteView(
                    uiState = uiState,
                    onEnterDashboard = {
                        viewModel.completeOnboarding(context, onNavigateToDashboard)
                    }
                )
            }
        }
    }
}

@Composable
private fun WelcomeStepView(
    selectedLanguage: String,
    onSelectLanguage: (String) -> Unit,
    onStartPlacement: () -> Unit,
    onStartScratch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "ODER",
                style = OderTypography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp,
                    fontSize = 38.sp
                ),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Master German & Polish with confidence",
                style = OderTypography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Language Selection
            Text(
                text = "CHOOSE YOUR LANGUAGE",
                style = OderTypography.labelSmall.copy(letterSpacing = 1.5.sp),
                color = TextTertiary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LanguageSelectionCard(
                    title = "Deutsch",
                    subtitle = "Cases & Prepositions",
                    badge = "DE",
                    isSelected = selectedLanguage == "de",
                    onClick = { onSelectLanguage("de") },
                    modifier = Modifier.weight(1f)
                )
                LanguageSelectionCard(
                    title = "Polski",
                    subtitle = "Aspects & Declensions",
                    badge = "PL",
                    isSelected = selectedLanguage == "pl",
                    onClick = { onSelectLanguage("pl") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Flow Options
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OnboardingOptionCard(
                title = "Quick Placement Check",
                description = "3 short questions to customize your starting review plan.",
                icon = Icons.Default.School,
                isPrimary = true,
                onClick = onStartPlacement
            )

            OnboardingOptionCard(
                title = "Start from the Beginning",
                description = "Begin with foundational vocabulary and grammar patterns.",
                icon = Icons.Default.PlayArrow,
                isPrimary = false,
                onClick = onStartScratch
            )
        }
    }
}

@Composable
private fun LanguageSelectionCard(
    title: String,
    subtitle: String,
    badge: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "lang_card_press"
    )

    Column(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) DarkSurfaceElevated else DarkSurface)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) NounMasculine else DarkBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = OderTypography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) NounMasculine else DarkSurfaceVariant)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badge,
                    style = OderTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) OledBlack else TextSecondary
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = OderTypography.bodySmall,
            color = TextTertiary
        )
    }
}

@Composable
private fun OnboardingOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    isPrimary: Boolean,
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
        label = "option_card_scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(14.dp))
            .background(if (isPrimary) TextPrimary else DarkSurface)
            .border(1.dp, if (isPrimary) TextPrimary else DarkBorder, RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(if (isPrimary) OledBlack else DarkSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isPrimary) TextPrimary else NounMasculine,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = OderTypography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = if (isPrimary) OledBlack else TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = OderTypography.bodySmall,
                color = if (isPrimary) Color(0xFF333333) else TextTertiary
            )
        }
    }
}

@Composable
private fun DiagnosticStepView(
    uiState: OnboardingUiState,
    questions: List<DiagnosticQuestion>,
    onSelectOption: (Int) -> Unit,
    onSubmitAnswer: () -> Unit
) {
    val question = questions.getOrNull(uiState.currentQuestionIndex) ?: return
    val progress = (uiState.currentQuestionIndex + 1).toFloat() / questions.size.toFloat()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PLACEMENT CHECK",
                style = OderTypography.labelSmall.copy(letterSpacing = 1.5.sp),
                color = NounMasculine
            )
            Text(
                text = "${uiState.currentQuestionIndex + 1} of ${questions.size}",
                style = OderTypography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = NounMasculine,
            trackColor = DarkSurfaceVariant
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Question Prompt Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurface)
                .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                .padding(22.dp)
        ) {
            Text(
                text = question.prompt,
                style = OderTypography.labelSmall,
                color = TextTertiary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = question.context,
                style = OderTypography.headlineMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 26.sp
                ),
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Select the best option:",
            style = OderTypography.labelMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        question.options.forEachIndexed { index, optionText ->
            val isSelected = uiState.selectedOptionIndex == index

            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.97f else 1f,
                animationSpec = spring(
                    stiffness = Spring.StiffnessLow,
                    dampingRatio = Spring.DampingRatioMediumBouncy
                ),
                label = "choice_scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) DarkSurfaceElevated else DarkSurface)
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) NounMasculine else DarkBorder,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onSelectOption(index) }
                    )
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .border(
                                1.5.dp,
                                if (isSelected) NounMasculine else DarkBorder,
                                CircleShape
                            )
                            .background(if (isSelected) NounMasculine else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(OledBlack)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = optionText,
                        style = OderTypography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = if (isSelected) TextPrimary else TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.weight(1f, fill = false))
        Spacer(modifier = Modifier.height(24.dp))

        // Submit Button
        val submitInteraction = remember { MutableInteractionSource() }
        val isSubmitPressed by submitInteraction.collectIsPressedAsState()
        val submitScale by animateFloatAsState(
            targetValue = if (isSubmitPressed && uiState.selectedOptionIndex != null) 0.96f else 1f,
            animationSpec = spring(
                stiffness = Spring.StiffnessLow,
                dampingRatio = Spring.DampingRatioMediumBouncy
            ),
            label = "diag_submit_scale"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = submitScale
                    scaleY = submitScale
                }
                .clip(RoundedCornerShape(12.dp))
                .background(if (uiState.selectedOptionIndex != null) TextPrimary else DarkSurfaceElevated)
                .clickable(
                    interactionSource = submitInteraction,
                    indication = null,
                    enabled = uiState.selectedOptionIndex != null,
                    onClick = onSubmitAnswer
                )
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (uiState.currentQuestionIndex + 1 == questions.size) "Complete Check" else "Next Question",
                style = OderTypography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = if (uiState.selectedOptionIndex != null) OledBlack else TextTertiary
            )
        }
    }
}

@Composable
private fun CalibrationCompleteView(
    uiState: OnboardingUiState,
    onEnterDashboard: () -> Unit
) {
    val enterInteraction = remember { MutableInteractionSource() }
    val isEnterPressed by enterInteraction.collectIsPressedAsState()
    val enterScale by animateFloatAsState(
        targetValue = if (isEnterPressed && !uiState.isSeeding) 0.96f else 1f,
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "enter_dash_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (uiState.isSeeding) {
            CircularProgressIndicator(
                color = NounMasculine,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Preparing your study material...",
                style = OderTypography.headlineSmall,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Setting up your personal vocabulary",
                style = OderTypography.bodySmall,
                color = TextTertiary
            )
        } else {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(AccentSuccess.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = AccentSuccess,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "You're All Set!",
                style = OderTypography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ready to practice ${if (uiState.selectedLanguage == "de") "German" else "Polish"}",
                style = OderTypography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Summary card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Starting Review Interval:",
                        style = OderTypography.bodySmall,
                        color = TextTertiary
                    )
                    Text(
                        text = "${uiState.startingIntervalDays} days",
                        style = OderTypography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = NounNeuter
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Starting Level:",
                        style = OderTypography.bodySmall,
                        color = TextTertiary
                    )
                    Text(
                        text = uiState.startingLevelLabel,
                        style = OderTypography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = NounMasculine
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Study Material:",
                        style = OderTypography.bodySmall,
                        color = TextTertiary
                    )
                    Text(
                        text = "Ready (Offline)",
                        style = OderTypography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = AccentSuccess
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Enter Dashboard CTA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = enterScale
                        scaleY = enterScale
                    }
                    .clip(RoundedCornerShape(12.dp))
                    .background(TextPrimary)
                    .clickable(
                        interactionSource = enterInteraction,
                        indication = null,
                        onClick = onEnterDashboard
                    )
                    .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Go to Dashboard",
            style = OderTypography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = OledBlack
        )
    }
}
    }
}
