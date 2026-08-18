package com.oder.domain.usecase

import android.content.Context
import com.oder.domain.model.GrammarModule
import com.oder.domain.model.GrammarRule
import com.oder.domain.model.Lexeme
import com.oder.domain.model.SrsState
import com.oder.domain.repository.LanguageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class SeedData(
    val lexemes: List<SeedLexeme> = emptyList(),
    val grammarModules: List<SeedGrammarModule> = emptyList(),
    val grammarRules: List<SeedGrammarRule> = emptyList()
)

@Serializable
data class SeedLexeme(
    val id: String,
    val rootWord: String,
    val language: String,
    val wordType: String,
    val grammarRequirements: String,
    val skillType: String = "general",
    val initialDifficulty: Double = 5.0,
    val initialStability: Double = 2.0
)

@Serializable
data class SeedGrammarModule(
    val id: String,
    val title: String,
    val content: String,
    val isMastered: Boolean = false,
    val language: String
)

@Serializable
data class SeedGrammarRule(
    val id: String,
    val moduleId: String,
    val targetWord: String,
    val conditionCheck: String,
    val errorMessage: String
)

data class SeedResult(
    val lexemesInserted: Int,
    val modulesInserted: Int,
    val rulesInserted: Int
)

class SeedDatabaseUseCase(
    private val repository: LanguageRepository
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Reads seed_data.json from Android assets and populates the database via repository.
     */
    suspend operator fun invoke(
        context: Context,
        fileName: String = "seed_data.json"
    ): Result<SeedResult> = withContext(Dispatchers.IO) {
        runCatching {
            val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            seedFromJson(jsonString)
        }
    }

    /**
     * Seeds database directly from a raw JSON string.
     */
    suspend fun seedFromJson(jsonString: String): SeedResult = withContext(Dispatchers.IO) {
        val seedData = json.decodeFromString<SeedData>(jsonString)
        val now = System.currentTimeMillis()

        // 1. Map and insert Lexemes + their initial SRS states
        val domainLexemes = ArrayList<Lexeme>(seedData.lexemes.size)
        val initialSrsStates = ArrayList<SrsState>(seedData.lexemes.size)

        for (item in seedData.lexemes) {
            domainLexemes.add(
                Lexeme(
                    id = item.id,
                    rootWord = item.rootWord,
                    language = item.language,
                    wordType = item.wordType,
                    grammarRequirements = item.grammarRequirements
                )
            )
            initialSrsStates.add(
                SrsState(
                    wordId = item.id,
                    difficulty = item.initialDifficulty,
                    stability = item.initialStability,
                    retrievability = 0.9,
                    lastReviewDate = now,
                    nextReviewDate = now, // Due immediately for initial training
                    repetitionCount = 0,
                    skillType = item.skillType
                )
            )
        }

        if (domainLexemes.isNotEmpty()) {
            repository.insertLexemes(domainLexemes)
            repository.insertSrsStates(initialSrsStates)
        }

        // 2. Map and insert Grammar Modules
        val domainModules = seedData.grammarModules.map { module ->
            GrammarModule(
                id = module.id,
                title = module.title,
                content = module.content,
                isMastered = module.isMastered,
                language = module.language
            )
        }
        if (domainModules.isNotEmpty()) {
            repository.insertGrammarModules(domainModules)
        }

        // 3. Map and insert Grammar Rules
        val domainRules = seedData.grammarRules.map { rule ->
            GrammarRule(
                id = rule.id,
                moduleId = rule.moduleId,
                targetWord = rule.targetWord,
                conditionCheck = rule.conditionCheck,
                errorMessage = rule.errorMessage
            )
        }
        if (domainRules.isNotEmpty()) {
            repository.insertGrammarRules(domainRules)
        }

        SeedResult(
            lexemesInserted = domainLexemes.size,
            modulesInserted = domainModules.size,
            rulesInserted = domainRules.size
        )
    }
}
