package com.oder.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.oder.presentation.dashboard.DashboardScreen
import com.oder.presentation.engine.ReviewScreen
import com.oder.presentation.onboarding.OnboardingScreen
import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Onboarding : Screen

    @Serializable
    data object Dashboard : Screen

    @Serializable
    data class ReviewSession(val language: String = "de") : Screen
}

@Composable
fun OderNavGraph(
    navController: NavHostController,
    hasCompletedOnboarding: Boolean,
    modifier: Modifier = Modifier
) {
    val startDestination: Screen = if (hasCompletedOnboarding) Screen.Dashboard else Screen.Onboarding

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            fadeIn(
                animationSpec = spring(
                    stiffness = Spring.StiffnessLow,
                    dampingRatio = Spring.DampingRatioMediumBouncy
                )
            ) + slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = spring(
                    stiffness = Spring.StiffnessLow,
                    dampingRatio = Spring.DampingRatioMediumBouncy
                )
            )
        },
        exitTransition = {
            fadeOut(
                animationSpec = spring(
                    stiffness = Spring.StiffnessLow,
                    dampingRatio = Spring.DampingRatioMediumBouncy
                )
            ) + slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = spring(
                    stiffness = Spring.StiffnessLow,
                    dampingRatio = Spring.DampingRatioMediumBouncy
                )
            )
        },
        popEnterTransition = {
            fadeIn(
                animationSpec = spring(
                    stiffness = Spring.StiffnessLow,
                    dampingRatio = Spring.DampingRatioMediumBouncy
                )
            ) + slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = spring(
                    stiffness = Spring.StiffnessLow,
                    dampingRatio = Spring.DampingRatioMediumBouncy
                )
            )
        },
        popExitTransition = {
            fadeOut(
                animationSpec = spring(
                    stiffness = Spring.StiffnessLow,
                    dampingRatio = Spring.DampingRatioMediumBouncy
                )
            ) + slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = spring(
                    stiffness = Spring.StiffnessLow,
                    dampingRatio = Spring.DampingRatioMediumBouncy
                )
            )
        }
    ) {
        composable<Screen.Onboarding> {
            OnboardingScreen(
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard) {
                        popUpTo(Screen.Onboarding) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Dashboard> {
            DashboardScreen(
                onNavigateToReview = { language ->
                    navController.navigate(Screen.ReviewSession(language = language))
                }
            )
        }

        composable<Screen.ReviewSession> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.ReviewSession>()
            ReviewScreen(
                language = route.language,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
