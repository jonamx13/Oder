package com.oder.presentation.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oder.core.util.UserPreferencesRepository
import com.oder.domain.repository.LanguageRepository
import com.oder.domain.usecase.SeedDatabaseUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class OnboardingStep {
    WELCOME,
    DIAGNOSTIC,
    CALIBRATION_COMPLETE
}

data class DiagnosticQuestion(
    val id: String,
    val prompt: String,
    val context: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.WELCOME,
    val selectedLanguage: String = "de",
    val isPlacementTest: Boolean = false,
    val currentQuestionIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val correctAnswersCount: Int = 0,
    val isSeeding: Boolean = false,
    val isCompleted: Boolean = false,
    val startingLevelLabel: String = "Intermediate",
    val startingIntervalDays: Int = 3
)

class OnboardingViewModel(
    private val repository: LanguageRepository? = null,
    private val seedDatabaseUseCase: SeedDatabaseUseCase? = null,
    private val userPreferencesRepository: UserPreferencesRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun selectLanguage(language: String) {
        _uiState.update { it.copy(selectedLanguage = language) }
    }

    fun startPlacementTest() {
        _uiState.update {
            it.copy(
                step = OnboardingStep.DIAGNOSTIC,
                isPlacementTest = true,
                currentQuestionIndex = 0,
                selectedOptionIndex = null,
                correctAnswersCount = 0
            )
        }
    }

    fun startFromScratch(context: Context) {
        _uiState.update {
            it.copy(
                step = OnboardingStep.CALIBRATION_COMPLETE,
                isPlacementTest = false,
                startingLevelLabel = "Foundational",
                startingIntervalDays = 2
            )
        }
        seedBaselineDatabase(context)
    }

    fun selectOption(index: Int) {
        _uiState.update { it.copy(selectedOptionIndex = index) }
    }

    fun submitAnswer(context: Context) {
        val currentState = _uiState.value
        val selected = currentState.selectedOptionIndex ?: return
        val questions = getQuestions(currentState.selectedLanguage)
        val currentQuestion = questions.getOrNull(currentState.currentQuestionIndex) ?: return

        val isCorrect = selected == currentQuestion.correctIndex
        val updatedCorrectCount = if (isCorrect) currentState.correctAnswersCount + 1 else currentState.correctAnswersCount

        val nextIndex = currentState.currentQuestionIndex + 1
        if (nextIndex >= questions.size) {
            val levelLabel = when (updatedCorrectCount) {
                3 -> "Advanced"
                2 -> "Intermediate"
                else -> "Foundational"
            }
            val intervalDays = when (updatedCorrectCount) {
                3 -> 5
                2 -> 3
                else -> 1
            }

            _uiState.update {
                it.copy(
                    step = OnboardingStep.CALIBRATION_COMPLETE,
                    correctAnswersCount = updatedCorrectCount,
                    startingLevelLabel = levelLabel,
                    startingIntervalDays = intervalDays
                )
            }
            seedBaselineDatabase(context)
        } else {
            _uiState.update {
                it.copy(
                    currentQuestionIndex = nextIndex,
                    selectedOptionIndex = null,
                    correctAnswersCount = updatedCorrectCount
                )
            }
        }
    }

    fun completeOnboarding(context: Context, onNavigate: () -> Unit) {
        viewModelScope.launch {
            val prefs = userPreferencesRepository ?: UserPreferencesRepository(context.applicationContext)
            prefs.setOnboardingCompleted(true)
            onNavigate()
        }
    }

    private fun seedBaselineDatabase(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSeeding = true) }
            val seeder = seedDatabaseUseCase ?: (repository?.let { SeedDatabaseUseCase(it) })
            seeder?.invoke(context)
            _uiState.update { it.copy(isSeeding = false, isCompleted = true) }
        }
    }

    fun getQuestions(language: String): List<DiagnosticQuestion> {
        return if (language == "de") {
            listOf(
                DiagnosticQuestion(
                    id = "de_q1",
                    prompt = "Genitive Prepositions",
                    context = "Wir spazieren im Park, trotz ___ starken Regens.",
                    options = listOf("dem", "des", "den", "das"),
                    correctIndex = 1,
                    explanation = "'trotz' always takes the genitive case ('des starken Regens')."
                ),
                DiagnosticQuestion(
                    id = "de_q2",
                    prompt = "Verbs with Fixed Prepositions",
                    context = "Die Teilnehmer warten ungeduldig ___ die Ergebnisse.",
                    options = listOf("auf", "an", "nach", "von"),
                    correctIndex = 0,
                    explanation = "'warten auf' takes the preposition 'auf' + accusative."
                ),
                DiagnosticQuestion(
                    id = "de_q3",
                    prompt = "Noun Gender & Endings",
                    context = "Eine weitreichende ___ wurde einstimmig beschlossen.",
                    options = listOf("der Beschluss", "die Entscheidung", "das Resultat", "den Vorschlag"),
                    correctIndex = 1,
                    explanation = "'Eine weitreichende' matches feminine noun 'die Entscheidung'."
                )
            )
        } else {
            listOf(
                DiagnosticQuestion(
                    id = "pl_q1",
                    prompt = "Verb Aspect",
                    context = "Wczoraj wreszcie ___ cały trudny raport.",
                    options = listOf("pisałem", "napisałem", "pisać", "pisano"),
                    correctIndex = 1,
                    explanation = "Completed action requires perfective aspect ('napisałem')."
                ),
                DiagnosticQuestion(
                    id = "pl_q2",
                    prompt = "Instrumental Case",
                    context = "Mój brat jest znakomitym ___.",
                    options = listOf("inżynier", "inżyniera", "inżynierem", "inżynierowi"),
                    correctIndex = 2,
                    explanation = "Profession after 'być' requires the instrumental case (-em)."
                ),
                DiagnosticQuestion(
                    id = "pl_q3",
                    prompt = "Grammatical Gender",
                    context = "Ta nowa ___ została wydana w Krakowie.",
                    options = listOf("podręcznik", "książka", "opowiadanie", "artykuł"),
                    correctIndex = 1,
                    explanation = "Demonstrative 'Ta nowa' matches feminine noun 'książka'."
                )
            )
        }
    }
}
