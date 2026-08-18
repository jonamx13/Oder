package com.oder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grammar_modules")
data class GrammarModuleEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val isMastered: Boolean = false,
    val language: String
)
