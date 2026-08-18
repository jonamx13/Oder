package com.oder.domain.repository

import com.oder.domain.model.GrammarModule
import com.oder.domain.model.GrammarRule
import com.oder.domain.model.Lexeme
import com.oder.domain.model.ReviewCard
import com.oder.domain.model.SrsState
import kotlinx.coroutines.flow.Flow

interface LanguageRepository {

    // Lexeme operations
    fun getAllLexemes(): Flow<List<Lexeme>>
    fun getLexemesByLanguage(language: String): Flow<List<Lexeme>>
    suspend fun getLexemeById(id: String): Lexeme?
    fun getDueLexemes(currentDate: Long): Flow<List<Lexeme>>
    fun getDueLexemesByLanguage(currentDate: Long, language: String): Flow<List<Lexeme>>
    suspend fun insertLexeme(lexeme: Lexeme)
    suspend fun insertLexemes(lexemes: List<Lexeme>)

    // SRS State operations
    suspend fun getSrsState(wordId: String): SrsState?
    fun getSrsStateFlow(wordId: String): Flow<SrsState?>
    fun getAllSrsStates(): Flow<List<SrsState>>
    suspend fun updateSrsState(srsState: SrsState)
    suspend fun insertSrsStates(srsStates: List<SrsState>)

    // Combined ReviewCard operations
    fun getDueReviewCards(currentDate: Long, language: String): Flow<List<ReviewCard>>
    suspend fun getReviewCard(wordId: String): ReviewCard?

    // Grammar operations
    fun getAllGrammarModules(): Flow<List<GrammarModule>>
    fun getGrammarModulesByLanguage(language: String): Flow<List<GrammarModule>>
    suspend fun getGrammarModuleById(id: String): GrammarModule?
    fun getGrammarRulesForModule(moduleId: String): Flow<List<GrammarRule>>
    suspend fun getGrammarRulesForTargetWord(targetWord: String): List<GrammarRule>
    suspend fun insertGrammarModules(modules: List<GrammarModule>)
    suspend fun insertGrammarRules(rules: List<GrammarRule>)
    suspend fun updateGrammarModule(module: GrammarModule)
}
