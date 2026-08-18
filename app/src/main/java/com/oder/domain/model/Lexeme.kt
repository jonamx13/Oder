package com.oder.domain.model

data class Lexeme(
    val id: String,
    val rootWord: String,
    val language: String,
    val wordType: String,
    val grammarRequirements: String
)
