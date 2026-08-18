package com.oder.domain.model

data class GrammarModule(
    val id: String,
    val title: String,
    val content: String,
    val isMastered: Boolean = false,
    val language: String
)
