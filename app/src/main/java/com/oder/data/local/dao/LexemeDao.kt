package com.oder.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.oder.data.local.entity.LexemeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LexemeDao {

    @Query("SELECT * FROM lexemes")
    fun getAllLexemes(): Flow<List<LexemeEntity>>

    @Query("SELECT * FROM lexemes WHERE language = :language")
    fun getLexemesByLanguage(language: String): Flow<List<LexemeEntity>>

    @Query("SELECT * FROM lexemes WHERE id = :id")
    suspend fun getLexemeById(id: String): LexemeEntity?

    @Query("""
        SELECT l.* FROM lexemes l
        INNER JOIN srs_states s ON l.id = s.wordId
        WHERE s.nextReviewDate <= :currentDate
    """)
    fun getDueLexemes(currentDate: Long): Flow<List<LexemeEntity>>

    @Query("""
        SELECT l.* FROM lexemes l
        INNER JOIN srs_states s ON l.id = s.wordId
        WHERE s.nextReviewDate <= :currentDate AND l.language = :language
    """)
    fun getDueLexemesByLanguage(currentDate: Long, language: String): Flow<List<LexemeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLexeme(lexeme: LexemeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLexemes(lexemes: List<LexemeEntity>)

    @Update
    suspend fun updateLexeme(lexeme: LexemeEntity)

    @Delete
    suspend fun deleteLexeme(lexeme: LexemeEntity)

    @Query("DELETE FROM lexemes")
    suspend fun deleteAllLexemes()
}
