package com.oder.domain.usecase

import com.oder.domain.model.ReviewCard
import com.oder.domain.repository.LanguageRepository
import kotlinx.coroutines.flow.Flow

class GetDailyQueueUseCase(
    private val repository: LanguageRepository
) {
    operator fun invoke(
        language: String,
        currentDate: Long = System.currentTimeMillis()
    ): Flow<List<ReviewCard>> {
        return repository.getDueReviewCards(currentDate, language)
    }
}
