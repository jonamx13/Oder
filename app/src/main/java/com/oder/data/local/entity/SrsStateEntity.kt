package com.oder.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "srs_states",
    foreignKeys = [
        ForeignKey(
            entity = LexemeEntity::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["wordId"])
    ]
)
data class SrsStateEntity(
    @PrimaryKey
    val wordId: String,
    val difficulty: Double,
    val stability: Double,
    val retrievability: Double,
    val lastReviewDate: Long,
    val nextReviewDate: Long,
    val repetitionCount: Int,
    val skillType: String
)
