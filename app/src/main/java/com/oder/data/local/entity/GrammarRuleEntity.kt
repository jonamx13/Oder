package com.oder.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "grammar_rules",
    foreignKeys = [
        ForeignKey(
            entity = GrammarModuleEntity::class,
            parentColumns = ["id"],
            childColumns = ["moduleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["moduleId"])
    ]
)
data class GrammarRuleEntity(
    @PrimaryKey
    val id: String,
    val moduleId: String,
    val targetWord: String,
    val conditionCheck: String,
    val errorMessage: String
)
