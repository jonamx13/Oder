package com.oder.domain.model

data class SrsState(
    val wordId: String,
    val difficulty: Double,
    val stability: Double,
    val retrievability: Double,
    val lastReviewDate: Long,
    val nextReviewDate: Long,
    val repetitionCount: Int,
    val skillType: String
)
