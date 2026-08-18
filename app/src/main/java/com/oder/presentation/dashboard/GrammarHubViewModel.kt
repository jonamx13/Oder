package com.oder.presentation.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.oder.data.local.OderDatabase
import com.oder.data.repository.LanguageRepositoryImpl
import com.oder.domain.model.GrammarModule
import com.oder.domain.model.GrammarRule
import com.oder.domain.repository.LanguageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GrammarModuleWithRules(
    val module: GrammarModule,
    val rules: List<GrammarRule> = emptyList()
)

data class GrammarHubUiState(
    val selectedLanguage: String = "de",
    val modules: List<GrammarModuleWithRules> = emptyList(),
    val masteredCount: Int = 0,
    val totalCount: Int = 0,
    val isLoading: Boolean = false
)

class GrammarHubViewModel(
    private val repository: LanguageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GrammarHubUiState())
    val uiState: StateFlow<GrammarHubUiState> = _uiState.asStateFlow()

    init {
        loadModules("de")
    }

    fun selectLanguage(language: String) {
        if (_uiState.value.selectedLanguage == language) return
        _uiState.update { it.copy(selectedLanguage = language) }
        loadModules(language)
    }

    fun toggleMastery(moduleId: String) {
        viewModelScope.launch {
            val moduleItem = _uiState.value.modules.firstOrNull { it.module.id == moduleId } ?: return@launch
            val updated = moduleItem.module.copy(isMastered = !moduleItem.module.isMastered)
            repository.updateGrammarModule(updated)
        }
    }

    private fun loadModules(language: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getGrammarModulesByLanguage(language).collect { modules ->
                val effectiveModules = if (modules.isEmpty()) getFallbackModules(language) else modules

                val listWithRules = effectiveModules.map { mod ->
                    val rules = repository.getGrammarRulesForTargetWord(mod.title)
                    GrammarModuleWithRules(module = mod, rules = rules)
                }

                val mastered = listWithRules.count { it.module.isMastered }

                _uiState.update {
                    it.copy(
                        selectedLanguage = language,
                        modules = listWithRules,
                        masteredCount = mastered,
                        totalCount = listWithRules.size,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun getFallbackModules(language: String): List<GrammarModule> {
        return if (language == "de") {
            listOf(
                GrammarModule(
                    id = "de_mod_dative_prep",
                    language = "de",
                    title = "Dative Prepositions (aus, bei, mit, nach, seit, von, zu)",
                    content = "These prepositions always force the following noun into the Dative case. Masculine 'der' becomes 'dem', feminine 'die' becomes 'der', neuter 'das' becomes 'dem'.",
                    isMastered = true
                ),
                GrammarModule(
                    id = "de_mod_accusative_prep",
                    language = "de",
                    title = "Accusative Prepositions (bis, durch, für, gegen, ohne, um)",
                    content = "Always take the Accusative case regardless of movement or rest. Masculine 'der' becomes 'den'.",
                    isMastered = false
                ),
                GrammarModule(
                    id = "de_mod_genitive_prep",
                    language = "de",
                    title = "Genitive Prepositions (während, trotz, wegen, statt)",
                    content = "High-register B2 prepositions requiring the Genitive case ('des Regens', 'der Situation').",
                    isMastered = false
                ),
                GrammarModule(
                    id = "de_mod_verb_rektion",
                    language = "de",
                    title = "Verbs with Fixed Prepositions (warten auf, abhängen von)",
                    content = "Key verbs that govern specific prepositions: 'warten auf' + Accusative, 'abhängen von' + Dative.",
                    isMastered = false
                ),
                GrammarModule(
                    id = "de_mod_adjective_declension",
                    language = "de",
                    title = "Adjective Declensions (Strong, Weak & Mixed)",
                    content = "Adjective ending rules for definite, indefinite, and zero article structures.",
                    isMastered = false
                ),
                GrammarModule(
                    id = "de_mod_passive_voice",
                    language = "de",
                    title = "Passive Voice & Alternatives (sein zu + Infinitiv)",
                    content = "Passive structures formed with 'werden + Partizip II' and formal modal substitutes.",
                    isMastered = false
                )
            )
        } else {
            listOf(
                GrammarModule(
                    id = "pl_mod_aspect_pairs",
                    language = "pl",
                    title = "Aspect Pairs (Dokonany vs Niedokonany)",
                    content = "Imperfective denotes ongoing or habitual actions; perfective denotes completed results.",
                    isMastered = true
                ),
                GrammarModule(
                    id = "pl_mod_instrumental_case",
                    language = "pl",
                    title = "Instrumental Case (Narzędnik: -em, -ą, -ami)",
                    content = "Required for professions after 'być' (On jest inżynierem) and with preposition 'z' (with).",
                    isMastered = false
                ),
                GrammarModule(
                    id = "pl_mod_locative_case",
                    language = "pl",
                    title = "Locative Case (Miejscownik: w, na, po, o, przy)",
                    content = "Used exclusively with location and topic prepositions with noun endings in -e or -u.",
                    isMastered = false
                ),
                GrammarModule(
                    id = "pl_mod_genitive_negation",
                    language = "pl",
                    title = "Genitive with Negated Verbs (Dopełniacz)",
                    content = "Negating an accusative verb automatically shifts its direct object into the Genitive case.",
                    isMastered = false
                ),
                GrammarModule(
                    id = "pl_mod_numeral_governance",
                    language = "pl",
                    title = "Numeral Governance (2-4 vs 5+)",
                    content = "Numbers 2-4 govern the nominative plural, while 5 and higher govern the genitive plural.",
                    isMastered = false
                )
            )
        }
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
            return GrammarHubViewModel(repository) as T
        }
    }
}
