package com.oder.presentation.dashboard

import androidx.lifecycle.ViewModel
import com.oder.domain.repository.LanguageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val badge: String,
    val subtitle: String
) {
    GERMAN("de", "Deutsch", "DE", "Cases & Verb Prepositions"),
    POLISH("pl", "Polski", "PL", "Aspects & Case Declensions")
}

data class GrammarMatrixItem(
    val id: String,
    val title: String,
    val caseOrTopic: String,
    val masteryRate: Float,
    val totalRules: Int,
    val activeRules: Int
)

data class DashboardUiState(
    val selectedLanguage: AppLanguage = AppLanguage.GERMAN,
    val dueCardsCount: Int = 18,
    val totalLexemes: Int = 420,
    val matrices: List<GrammarMatrixItem> = emptyList(),
    val isLoading: Boolean = false
)

class DashboardViewModel(
    private val repository: LanguageRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData(AppLanguage.GERMAN)
    }

    fun selectLanguage(language: AppLanguage) {
        _uiState.update { it.copy(selectedLanguage = language) }
        loadDashboardData(language)
    }

    private fun loadDashboardData(language: AppLanguage) {
        val sampleMatrices = when (language) {
            AppLanguage.GERMAN -> listOf(
                GrammarMatrixItem(
                    id = "de_akk",
                    title = "Accusative Prepositions",
                    caseOrTopic = "bis, durch, für, gegen, ohne, um",
                    masteryRate = 0.85f,
                    totalRules = 12,
                    activeRules = 10
                ),
                GrammarMatrixItem(
                    id = "de_dat",
                    title = "Dative Prepositions",
                    caseOrTopic = "aus, bei, mit, nach, seit, von, zu",
                    masteryRate = 0.65f,
                    totalRules = 16,
                    activeRules = 10
                ),
                GrammarMatrixItem(
                    id = "de_gen",
                    title = "Genitive Prepositions",
                    caseOrTopic = "während, trotz, wegen, statt",
                    masteryRate = 0.40f,
                    totalRules = 8,
                    activeRules = 3
                ),
                GrammarMatrixItem(
                    id = "de_rek",
                    title = "Verbs with Fixed Prepositions",
                    caseOrTopic = "warten auf (+Akk), abhängen von (+Dat)",
                    masteryRate = 0.55f,
                    totalRules = 25,
                    activeRules = 14
                ),
                GrammarMatrixItem(
                    id = "de_adj",
                    title = "Adjective Endings",
                    caseOrTopic = "Strong, weak, and mixed declensions",
                    masteryRate = 0.70f,
                    totalRules = 18,
                    activeRules = 13
                ),
                GrammarMatrixItem(
                    id = "de_pas",
                    title = "Passive Voice Forms",
                    caseOrTopic = "Vorgangspassiv & Zustandspassiv structures",
                    masteryRate = 0.30f,
                    totalRules = 10,
                    activeRules = 3
                )
            )
            AppLanguage.POLISH -> listOf(
                GrammarMatrixItem(
                    id = "pl_asp",
                    title = "Perfective vs Imperfective Verbs",
                    caseOrTopic = "Aspect pairs (robić / zrobić, czytać / przeczytać)",
                    masteryRate = 0.75f,
                    totalRules = 20,
                    activeRules = 15
                ),
                GrammarMatrixItem(
                    id = "pl_nar",
                    title = "Instrumental Case (Narzędnik)",
                    caseOrTopic = "Used after 'być' and with 'z'",
                    masteryRate = 0.80f,
                    totalRules = 14,
                    activeRules = 11
                ),
                GrammarMatrixItem(
                    id = "pl_mie",
                    title = "Locative Case (Miejscownik)",
                    caseOrTopic = "Location prepositions (w, na, po, przy)",
                    masteryRate = 0.60f,
                    totalRules = 16,
                    activeRules = 10
                ),
                GrammarMatrixItem(
                    id = "pl_dop",
                    title = "Genitive with Negation (Dopełniacz)",
                    caseOrTopic = "Negated verbs and quantity expressions",
                    masteryRate = 0.45f,
                    totalRules = 22,
                    activeRules = 10
                ),
                GrammarMatrixItem(
                    id = "pl_cel",
                    title = "Dative Case (Celownik)",
                    caseOrTopic = "Indirect objects (dziękować, pomagać)",
                    masteryRate = 0.35f,
                    totalRules = 12,
                    activeRules = 4
                ),
                GrammarMatrixItem(
                    id = "pl_lic",
                    title = "Numbers with Nouns",
                    caseOrTopic = "Counting rules (2, 3, 4 vs 5+)",
                    masteryRate = 0.25f,
                    totalRules = 10,
                    activeRules = 2
                )
            )
        }

        _uiState.update {
            it.copy(
                matrices = sampleMatrices,
                dueCardsCount = if (language == AppLanguage.GERMAN) 18 else 12,
                totalLexemes = if (language == AppLanguage.GERMAN) 420 else 380
            )
        }
    }
}
