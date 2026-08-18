package com.oder.data.repository

import com.oder.data.local.dao.GrammarDao
import com.oder.data.local.dao.LexemeDao
import com.oder.data.local.dao.SrsStateDao
import com.oder.data.local.entity.GrammarModuleEntity
import com.oder.data.local.entity.GrammarRuleEntity
import com.oder.data.local.entity.LexemeEntity
import com.oder.data.local.entity.SrsStateEntity
import com.oder.domain.model.GrammarModule
import com.oder.domain.model.GrammarRule
import com.oder.domain.model.Lexeme
import com.oder.domain.model.ReviewCard
import com.oder.domain.model.SrsState
import com.oder.domain.repository.LanguageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LanguageRepositoryImpl(
    private val lexemeDao: LexemeDao,
    private val srsStateDao: SrsStateDao,
    private val grammarDao: GrammarDao
) : LanguageRepository {

    // --- Lexemes ---

    override fun getAllLexemes(): Flow<List<Lexeme>> {
        return lexemeDao.getAllLexemes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getLexemesByLanguage(language: String): Flow<List<Lexeme>> {
        return lexemeDao.getLexemesByLanguage(language).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getLexemeById(id: String): Lexeme? {
        return lexemeDao.getLexemeById(id)?.toDomain()
    }

    override fun getDueLexemes(currentDate: Long): Flow<List<Lexeme>> {
        return lexemeDao.getDueLexemes(currentDate).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getDueLexemesByLanguage(currentDate: Long, language: String): Flow<List<Lexeme>> {
        return lexemeDao.getDueLexemesByLanguage(currentDate, language).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertLexeme(lexeme: Lexeme) {
        lexemeDao.insertLexeme(lexeme.toEntity())
    }

    override suspend fun insertLexemes(lexemes: List<Lexeme>) {
        lexemeDao.insertLexemes(lexemes.map { it.toEntity() })
    }

    // --- SRS State ---

    override suspend fun getSrsState(wordId: String): SrsState? {
        return srsStateDao.getSrsStateByWordId(wordId)?.toDomain()
    }

    override fun getSrsStateFlow(wordId: String): Flow<SrsState?> {
        return srsStateDao.getSrsStateFlowByWordId(wordId).map { it?.toDomain() }
    }

    override fun getAllSrsStates(): Flow<List<SrsState>> {
        return srsStateDao.getAllSrsStates().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun updateSrsState(srsState: SrsState) {
        srsStateDao.insertOrUpdate(srsState.toEntity())
    }

    override suspend fun insertSrsStates(srsStates: List<SrsState>) {
        srsStateDao.insertOrUpdateAll(srsStates.map { it.toEntity() })
    }

    // --- Combined ReviewCard ---

    override fun getDueReviewCards(currentDate: Long, language: String): Flow<List<ReviewCard>> {
        return lexemeDao.getDueLexemesByLanguage(currentDate, language).map { entities ->
            entities.mapNotNull { lexemeEntity ->
                val srsEntity = srsStateDao.getSrsStateByWordId(lexemeEntity.id)
                srsEntity?.let {
                    ReviewCard(
                        lexeme = lexemeEntity.toDomain(),
                        srsState = it.toDomain()
                    )
                }
            }
        }
    }

    override suspend fun getReviewCard(wordId: String): ReviewCard? {
        val lexeme = lexemeDao.getLexemeById(wordId)?.toDomain() ?: return null
        val srsState = srsStateDao.getSrsStateByWordId(wordId)?.toDomain() ?: return null
        return ReviewCard(lexeme = lexeme, srsState = srsState)
    }

    // --- Grammar ---

    override fun getAllGrammarModules(): Flow<List<GrammarModule>> {
        return grammarDao.getAllModules().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getGrammarModulesByLanguage(language: String): Flow<List<GrammarModule>> {
        return grammarDao.getModulesByLanguage(language).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getGrammarModuleById(id: String): GrammarModule? {
        return grammarDao.getModuleById(id)?.toDomain()
    }

    override fun getGrammarRulesForModule(moduleId: String): Flow<List<GrammarRule>> {
        return grammarDao.getRulesForModule(moduleId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getGrammarRulesForTargetWord(targetWord: String): List<GrammarRule> {
        return grammarDao.getRulesForTargetWord(targetWord).map { it.toDomain() }
    }

    override suspend fun insertGrammarModules(modules: List<GrammarModule>) {
        grammarDao.insertModules(modules.map { it.toEntity() })
    }

    override suspend fun insertGrammarRules(rules: List<GrammarRule>) {
        grammarDao.insertRules(rules.map { it.toEntity() })
    }

    override suspend fun updateGrammarModule(module: GrammarModule) {
        grammarDao.updateModule(module.toEntity())
    }

    // --- Mapping Extensions ---

    private fun LexemeEntity.toDomain(): Lexeme = Lexeme(
        id = id,
        rootWord = rootWord,
        language = language,
        wordType = wordType,
        grammarRequirements = grammarRequirements
    )

    private fun Lexeme.toEntity(): LexemeEntity = LexemeEntity(
        id = id,
        rootWord = rootWord,
        language = language,
        wordType = wordType,
        grammarRequirements = grammarRequirements
    )

    private fun SrsStateEntity.toDomain(): SrsState = SrsState(
        wordId = wordId,
        difficulty = difficulty,
        stability = stability,
        retrievability = retrievability,
        lastReviewDate = lastReviewDate,
        nextReviewDate = nextReviewDate,
        repetitionCount = repetitionCount,
        skillType = skillType
    )

    private fun SrsState.toEntity(): SrsStateEntity = SrsStateEntity(
        wordId = wordId,
        difficulty = difficulty,
        stability = stability,
        retrievability = retrievability,
        lastReviewDate = lastReviewDate,
        nextReviewDate = nextReviewDate,
        repetitionCount = repetitionCount,
        skillType = skillType
    )

    private fun GrammarModuleEntity.toDomain(): GrammarModule = GrammarModule(
        id = id,
        title = title,
        content = content,
        isMastered = isMastered,
        language = language
    )

    private fun GrammarModule.toEntity(): GrammarModuleEntity = GrammarModuleEntity(
        id = id,
        title = title,
        content = content,
        isMastered = isMastered,
        language = language
    )

    private fun GrammarRuleEntity.toDomain(): GrammarRule = GrammarRule(
        id = id,
        moduleId = moduleId,
        targetWord = targetWord,
        conditionCheck = conditionCheck,
        errorMessage = errorMessage
    )

    private fun GrammarRule.toEntity(): GrammarRuleEntity = GrammarRuleEntity(
        id = id,
        moduleId = moduleId,
        targetWord = targetWord,
        conditionCheck = conditionCheck,
        errorMessage = errorMessage
    )
}
