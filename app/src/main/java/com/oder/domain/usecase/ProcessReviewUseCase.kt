package com.oder.domain.usecase

import com.oder.core.fsrs.FsrsAlgorithm
import com.oder.core.fsrs.Rating
import com.oder.domain.model.ReviewCard
import com.oder.domain.repository.LanguageRepository

class ProcessReviewUseCase(
    private val repository: LanguageRepository
) {
    suspend operator fun invoke(
        card: ReviewCard,
        rating: Rating,
        reviewTimestamp: Long = System.currentTimeMillis()
    ): ReviewCard {
        val updatedState = FsrsAlgorithm.calculateNextState(
            currentState = card.srsState,
            rating = rating,
            reviewTimeMs = reviewTimestamp
        )
        repository.updateSrsState(updatedState)
        return card.copy(srsState = updatedState)
    }
}
