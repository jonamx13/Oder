package com.oder.presentation.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.oder.data.local.OderDatabase
import com.oder.data.repository.LanguageRepositoryImpl
import com.oder.domain.model.Lexeme
import com.oder.domain.model.SrsState
import com.oder.domain.repository.LanguageRepository
import com.oder.domain.usecase.GetDailyQueueUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GrammarCategoryMastery(
    val categoryName: String,
    val description: String,
    val progress: Float, // 0.0f to 1.0f
    val reviewedCount: Int,
    val totalCount: Int
)

data class DashboardUiState(
    val selectedLanguage: String = "de", // "de" or "pl"
    val dueCardsCount: Int = 0,
    val totalVocabulary: Int = 0,
    val nounMastery: GrammarCategoryMastery = GrammarCategoryMastery("Nouns", "Gender & Plural Forms", 0f, 0, 0),
    val verbMastery: GrammarCategoryMastery = GrammarCategoryMastery("Verbs", "Aspects & Prepositional Verbs", 0f, 0, 0),
    val caseMastery: GrammarCategoryMastery = GrammarCategoryMastery("Cases", "Prepositions & Declensions", 0f, 0, 0),
    val isLoading: Boolean = false
)

class DashboardViewModel(
    private val repository: LanguageRepository,
    private val getDailyQueueUseCase: GetDailyQueueUseCase = GetDailyQueueUseCase(repository)
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeLanguageData("de")
    }

    fun selectLanguage(language: String) {
        if (_uiState.value.selectedLanguage == language) return
        _uiState.update { it.copy(selectedLanguage = language) }
        observeLanguageData(language)
    }

    private fun observeLanguageData(language: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val now = System.currentTimeMillis()

            combine(
                repository.getLexemesByLanguage(language),
                repository.getAllSrsStates(),
                getDailyQueueUseCase(language, now)
            ) { lexemes, srsStates, dueCards ->
                val srsMap = srsStates.associateBy { it.wordId }

                val nounLexemes = lexemes.filter { it.wordType.equals("Noun", ignoreCase = true) }
                val verbLexemes = lexemes.filter { it.wordType.equals("Verb", ignoreCase = true) }
                val caseLexemes = lexemes.filter {
                    it.wordType.equals("Preposition", ignoreCase = true) ||
                        it.grammarRequirements.contains("Kasus", ignoreCase = true) ||
                        it.grammarRequirements.contains("Akkusativ", ignoreCase = true) ||
                        it.grammarRequirements.contains("Dativ", ignoreCase = true) ||
                        it.grammarRequirements.contains("Genitiv", ignoreCase = true) ||
                        it.grammarRequirements.contains("Narzędnik", ignoreCase = true) ||
                        it.grammarRequirements.contains("Miejscownik", ignoreCase = true)
                }

                val nounProgress = calculateCategoryMastery(nounLexemes, srsMap, "Nouns", "Gender & Plural Forms")
                val verbProgress = calculateCategoryMastery(
                    verbLexemes,
                    srsMap,
                    "Verbs",
                    if (language == "de") "Prepositional Verbs & Conjugation" else "Aspect Pairs & Conjugation"
                )
                val caseProgress = calculateCategoryMastery(
                    caseLexemes,
                    srsMap,
                    "Cases",
                    if (language == "de") "Accusative, Dative & Genitive" else "Declensions & Cases"
                )

                DashboardUiState(
                    selectedLanguage = language,
                    dueCardsCount = dueCards.size,
                    totalVocabulary = lexemes.size,
                    nounMastery = nounProgress,
                    verbMastery = verbProgress,
                    caseMastery = caseProgress,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun calculateCategoryMastery(
        lexemes: List<Lexeme>,
        srsMap: Map<String, SrsState>,
        name: String,
        description: String
    ): GrammarCategoryMastery {
        if (lexemes.isEmpty()) {
            return GrammarCategoryMastery(name, description, 0f, 0, 0)
        }

        var reviewedCount = 0
        for (lexeme in lexemes) {
            val srs = srsMap[lexeme.id]
            if (srs != null && srs.repetitionCount > 0 && srs.stability >= 2.0) {
                reviewedCount++
            }
        }

        val progress = (reviewedCount.toFloat() / lexemes.size.toFloat()).coerceIn(0f, 1f)
        return GrammarCategoryMastery(
            categoryName = name,
            description = description,
            progress = progress,
            reviewedCount = reviewedCount,
            totalCount = lexemes.size
        )
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val database = OderDatabase.getInstance(context.applicationContext)
            val repository = LanguageRepositoryImpl(
                database.lexemeDao(),
                database.srsStateDao(),
                database.grammarDao()
            )
            return DashboardViewModel(repository) as T
        }
    }
}
