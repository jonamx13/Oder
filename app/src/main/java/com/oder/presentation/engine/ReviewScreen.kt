package com.oder.presentation.engine

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.oder.core.fsrs.Rating
import com.oder.core.theme.AccentError
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
import com.oder.domain.model.ReviewCard
import com.oder.presentation.engine.components.ConjugationInput
import com.oder.presentation.engine.components.GenderSelector
import com.oder.presentation.engine.components.InterceptorBottomSheet

@Composable
fun ReviewScreen(
    language: String = "de",
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ReviewViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(language) {
        viewModel.loadQueue(language)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OledBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (state.activeGrammarError != null) 100.dp else 0.dp)
        ) {
            // --- Top Bar ---
            ReviewTopBar(
                currentIndex = state.currentIndex,
                totalCards = state.queue.size,
                language = language,
                onNavigateBack = onNavigateBack
            )

            if (state.isSessionFinished) {
                SessionFinishedView(
                    reviewedCount = state.sessionReviewedCount,
                    onReturn = onNavigateBack
                )
            } else {
                state.currentCard?.let { card ->
                    ReviewContent(
                        card = card,
                        language = language,
                        userInput = state.userInput,
                        isAnswerRevealed = state.isAnswerRevealed,
                        isCorrect = state.isCorrect,
                        isError = state.activeGrammarError != null,
                        onInputChanged = { viewModel.handleInputChanged(it) },
                        onSubmit = { viewModel.submitAnswer() },
                        onRate = { rating -> viewModel.rateCard(rating) }
                    )
                }
            }
        }

        // --- Slide-up Interceptor Bottom Sheet Overlay ---
        InterceptorBottomSheet(
            activeGrammarError = state.activeGrammarError,
            onAcknowledge = { viewModel.onIntent(ReviewIntent.ResetError) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun ReviewTopBar(
    currentIndex: Int,
    totalCards: Int,
    language: String,
    onNavigateBack: () -> Unit
) {
    val backInteractionSource = remember { MutableInteractionSource() }
    val isBackPressed by backInteractionSource.collectIsPressedAsState()
    val backScale by animateFloatAsState(
        targetValue = if (isBackPressed) 0.92f else 1f,
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "back_btn_scale"
    )

    val progress = if (totalCards > 0) (currentIndex + 1).toFloat() / totalCards.toFloat() else 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = backScale
                        scaleY = backScale
                    }
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant)
                    .border(1.dp, DarkBorder, CircleShape)
                    .clickable(
                        interactionSource = backInteractionSource,
                        indication = null,
                        onClick = onNavigateBack
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Navigate Back",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Step Counter
            Text(
                text = if (totalCards > 0) "Word ${currentIndex + 1} of $totalCards" else "Queue Empty",
                style = OderTypography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = TextSecondary
            )

            // Language Tag
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant)
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = language.uppercase(),
                    style = OderTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = NounMasculine
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Progress Bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = NounMasculine,
            trackColor = DarkSurfaceVariant
        )
    }
}

@Composable
private fun ReviewContent(
    card: ReviewCard,
    language: String,
    userInput: String,
    isAnswerRevealed: Boolean,
    isCorrect: Boolean?,
    isError: Boolean,
    onInputChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onRate: (Rating) -> Unit
) {
    val isGenderCard = card.lexeme.wordType.equals("Noun", ignoreCase = true) ||
        card.srsState.skillType.equals("gender", ignoreCase = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Prompt Card
        CardPromptSection(card = card)

        // Dynamic Interactive Input Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (isGenderCard) "Select the correct article / gender:" else "Type the correct sentence form:",
                style = OderTypography.labelMedium,
                color = TextSecondary
            )

            if (isGenderCard) {
                GenderSelector(
                    selectedGender = userInput.ifBlank { null },
                    language = language,
                    onGenderSelected = { gender ->
                        onInputChanged(gender)
                        onSubmit()
                    },
                    enabled = !isAnswerRevealed
                )
            } else {
                ConjugationInput(
                    value = userInput,
                    onValueChange = onInputChanged,
                    onSubmit = onSubmit,
                    enabled = !isAnswerRevealed,
                    isError = isError
                )
            }
        }

        // Revealed Answer & Recall Rating Section
        AnimatedVisibility(
            visible = isAnswerRevealed,
            enter = fadeIn(spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)) +
                scaleIn(initialScale = 0.95f, animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)),
            exit = fadeOut(spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)) +
                scaleOut(targetScale = 0.95f, animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy))
        ) {
            RevealedAnswerSection(
                card = card,
                isCorrect = isCorrect == true,
                onRate = onRate
            )
        }
    }
}

@Composable
private fun CardPromptSection(card: ReviewCard) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .padding(22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Word Type Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceVariant)
                    .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = card.lexeme.wordType.uppercase(),
                    style = OderTypography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = NounMasculine
                )
            }

            // Learner-friendly memory strength indicator
            Text(
                text = "Memory Strength: ${"%.0f".format(card.srsState.stability)}d • Reviews: ${card.srsState.repetitionCount}",
                style = OderTypography.labelSmall,
                color = TextTertiary
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Target Root Prompt
        Text(
            text = card.lexeme.rootWord,
            style = OderTypography.displayMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Grammar Requirement / Rule Hint
        if (card.lexeme.grammarRequirements.isNotBlank()) {
            Text(
                text = card.lexeme.grammarRequirements,
                style = OderTypography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun RevealedAnswerSection(
    card: ReviewCard,
    isCorrect: Boolean,
    onRate: (Rating) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(
                1.dp,
                if (isCorrect) AccentSuccess.copy(alpha = 0.5f) else AccentError.copy(alpha = 0.5f),
                RoundedCornerShape(16.dp)
            )
            .padding(20.dp)
    ) {
        // Status Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                contentDescription = null,
                tint = if (isCorrect) AccentSuccess else AccentError,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = if (isCorrect) "Correct!" else "Needs Review",
                style = OderTypography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isCorrect) AccentSuccess else AccentError
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Correct Form:",
            style = OderTypography.labelSmall,
            color = TextTertiary
        )
        Text(
            text = card.lexeme.rootWord,
            style = OderTypography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "How well did you remember this?",
            style = OderTypography.labelSmall,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3 Recall Rating Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RatingButton(
                label = "Hard",
                sublabel = "Review soon",
                accentColor = NounFeminine,
                onClick = { onRate(Rating.HARD) },
                modifier = Modifier.weight(1f)
            )
            RatingButton(
                label = "Good",
                sublabel = "Remembered",
                accentColor = NounMasculine,
                onClick = { onRate(Rating.GOOD) },
                modifier = Modifier.weight(1f)
            )
            RatingButton(
                label = "Easy",
                sublabel = "Mastered",
                accentColor = NounNeuter,
                onClick = { onRate(Rating.EASY) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RatingButton(
    label: String,
    sublabel: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "rating_button_press"
    )

    Column(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = OderTypography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = accentColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = sublabel,
            style = OderTypography.labelSmall.copy(fontSize = 10.sp),
            color = TextTertiary
        )
    }
}

@Composable
private fun SessionFinishedView(
    reviewedCount: Int,
    onReturn: () -> Unit
) {
    val returnInteraction = remember { MutableInteractionSource() }
    val isPressed by returnInteraction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "return_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(AccentSuccess.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = AccentSuccess,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Practice Complete!",
            style = OderTypography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Great work! $reviewedCount words reviewed and updated.",
            style = OderTypography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(32.dp))

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
                    interactionSource = returnInteraction,
                    indication = null,
                    onClick = onReturn
                )
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Back to Dashboard",
                style = OderTypography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = OledBlack
            )
        }
    }
}
