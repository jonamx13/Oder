package com.oder.presentation.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val calibratedDifficulty: Double = 5.0,
    val calibratedStability: Double = 2.0
)

class OnboardingViewModel(
    private val repository: LanguageRepository? = null,
    private val seedDatabaseUseCase: SeedDatabaseUseCase? = null
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
                calibratedDifficulty = 5.0,
                calibratedStability = 2.0
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
            // Diagnostic complete -> compute calibrated FSRS baseline
            val finalDifficulty = when (updatedCorrectCount) {
                3 -> 3.5 // Advanced baseline
                2 -> 4.8 // Intermediate B2 baseline
                else -> 6.5 // Foundational B2 baseline
            }
            val finalStability = when (updatedCorrectCount) {
                3 -> 4.5
                2 -> 2.5
                else -> 1.2
            }

            _uiState.update {
                it.copy(
                    step = OnboardingStep.CALIBRATION_COMPLETE,
                    correctAnswersCount = updatedCorrectCount,
                    calibratedDifficulty = finalDifficulty,
                    calibratedStability = finalStability
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

    private fun seedBaselineDatabase(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSeeding = true) }
            seedDatabaseUseCase?.invoke(context)
            _uiState.update { it.copy(isSeeding = false, isCompleted = true) }
        }
    }

    fun getQuestions(language: String): List<DiagnosticQuestion> {
        return if (language == "de") {
            listOf(
                DiagnosticQuestion(
                    id = "de_q1",
                    prompt = "Kasus-Rektion (Genitiv)",
                    context = "Wir spazieren im Park, trotz ___ starken Regens.",
                    options = listOf("dem", "des", "den", "das"),
                    correctIndex = 1,
                    explanation = "'trotz' regiert immer den Genitiv (des starken Regens)."
                ),
                DiagnosticQuestion(
                    id = "de_q2",
                    prompt = "Verben mit Präpositionen",
                    context = "Die Teilnehmer warten ungeduldig ___ die Ergebnisse.",
                    options = listOf("auf", "an", "nach", "von"),
                    correctIndex = 0,
                    explanation = "'warten auf' erfordert die Präposition 'auf' mit Akkusativ."
                ),
                DiagnosticQuestion(
                    id = "de_q3",
                    prompt = "Genus & Deklination",
                    context = "Eine weitreichende ___ wurde einstimmig beschlossen.",
                    options = listOf("der Beschluss", "die Entscheidung", "das Resultat", "den Vorschlag"),
                    correctIndex = 1,
                    explanation = "'Eine weitreichende' verlangt ein femininum Substantiv (die Entscheidung)."
                )
            )
        } else {
            listOf(
                DiagnosticQuestion(
                    id = "pl_q1",
                    prompt = "Aspekt Czasownika",
                    context = "Wczoraj wreszcie ___ cały trudny raport.",
                    options = listOf("pisałem", "napisałem", "pisać", "pisano"),
                    correctIndex = 1,
                    explanation = "Ukończona czynność jednorazowa wymaga aspektu dokonanego (napisałem)."
                ),
                DiagnosticQuestion(
                    id = "pl_q2",
                    prompt = "Rządzenie Przypadkiem (Narzędnik)",
                    context = "Mój brat jest znakomitym ___.",
                    options = listOf("inżynier", "inżyniera", "inżynierem", "inżynierowi"),
                    correctIndex = 2,
                    explanation = "Konstrukcja 'być + rzeczownik' wymaga Narzędnika (-em)."
                ),
                DiagnosticQuestion(
                    id = "pl_q3",
                    prompt = "Rodzaj Gramatyczny",
                    context = "Ta nowa ___ została wydana w Krakowie.",
                    options = listOf("podręcznik", "książka", "opowiadanie", "artykuł"),
                    correctIndex = 1,
                    explanation = "Zaimek 'Ta nowa' wymaga rzeczownika rodzaju żeńskiego (-a)."
                )
            )
        }
    }
}
