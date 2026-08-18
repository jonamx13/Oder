package com.oder.domain.model

data class GrammarRule(
    val id: String,
    val moduleId: String,
    val targetWord: String,
    val conditionCheck: String,
    val errorMessage: String
)
