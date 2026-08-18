package com.oder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lexemes")
data class LexemeEntity(
    @PrimaryKey
    val id: String,
    val rootWord: String,
    val language: String,
    val wordType: String,
    val grammarRequirements: String
)
