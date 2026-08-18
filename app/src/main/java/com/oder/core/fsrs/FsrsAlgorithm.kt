package com.oder.core.fsrs

import com.oder.domain.model.SrsState
import kotlin.math.max
import kotlin.math.pow

object FsrsAlgorithm {

    private const val ONE_DAY_MS = 86_400_000L

    /**
     * Calculates the memory retrievability R given elapsed days t and stability S.
     * Uses the power-law forgetting curve: R(t, S) = (1 + t / (9 * S))^-1
     */
    fun calculateRetrievability(elapsedDays: Double, stability: Double): Double {
        if (stability <= 0.0) return 0.0
        return (1.0 + elapsedDays / (9.0 * stability)).pow(-1.0).coerceIn(0.0, 1.0)
    }

    /**
     * Computes the updated Difficulty, Stability, Retrievability, and nextReviewDate
     * based on the user's rating (Hard, Good, Easy).
     */
    fun calculateNextState(
        currentState: SrsState,
        rating: Rating,
        reviewTimeMs: Long = System.currentTimeMillis()
    ): SrsState {
        val lastReview = if (currentState.lastReviewDate <= 0L) reviewTimeMs else currentState.lastReviewDate
        val elapsedDays = max(0.0, (reviewTimeMs - lastReview).toDouble() / ONE_DAY_MS)
        val currentRetrievability = calculateRetrievability(elapsedDays, currentState.stability)

        val isInitial = currentState.repetitionCount == 0 || currentState.stability <= 0.0

        val newDifficulty: Double
        val newStability: Double

        if (isInitial) {
            when (rating) {
                Rating.HARD -> {
                    newDifficulty = 7.5
                    newStability = 1.0
                }
                Rating.GOOD -> {
                    newDifficulty = 5.0
                    newStability = 3.0
                }
                Rating.EASY -> {
                    newDifficulty = 3.0
                    newStability = 6.0
                }
            }
        } else {
            // Update difficulty: D' = clamp(D + delta_D, 1.0, 10.0)
            val dDelta = when (rating) {
                Rating.HARD -> 1.2
                Rating.GOOD -> -0.2
                Rating.EASY -> -1.0
            }
            newDifficulty = (currentState.difficulty + dDelta).coerceIn(1.0, 10.0)

            // Update stability based on rating and retrievability
            newStability = when (rating) {
                Rating.HARD -> {
                    max(1.0, currentState.stability * 0.75)
                }
                Rating.GOOD -> {
                    val factor = 1.0 + (11.0 - newDifficulty) / 5.0 * (1.0 + (1.0 - currentRetrievability))
                    max(1.0, currentState.stability * factor)
                }
                Rating.EASY -> {
                    val factor = 1.3 + (11.0 - newDifficulty) / 4.0 * (1.0 + (1.0 - currentRetrievability))
                    max(1.0, currentState.stability * factor)
                }
            }
        }

        // Interval in days matches stability for 90% target retention
        val intervalDays = max(1.0, newStability)
        val nextReviewDate = reviewTimeMs + (intervalDays * ONE_DAY_MS).toLong()
        val newRetrievability = calculateRetrievability(0.0, newStability)

        return currentState.copy(
            difficulty = newDifficulty,
            stability = newStability,
            retrievability = newRetrievability,
            lastReviewDate = reviewTimeMs,
            nextReviewDate = nextReviewDate,
            repetitionCount = currentState.repetitionCount + 1
        )
    }
}
