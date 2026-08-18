package com.oder.presentation.engine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oder.core.fsrs.Rating
import com.oder.domain.model.Lexeme
import com.oder.domain.model.ReviewCard
import com.oder.domain.model.SrsState
import com.oder.domain.repository.LanguageRepository
import com.oder.domain.usecase.EvaluateReviewUseCase
import com.oder.domain.usecase.GetDailyQueueUseCase
import com.oder.domain.usecase.ProcessReviewUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReviewState(
    val language: String = "de",
    val queue: List<ReviewCard> = emptyList(),
    val currentIndex: Int = 0,
    val currentCard: ReviewCard? = null,
    val userInput: String = "",
    val isAnswerRevealed: Boolean = false,
    val isCorrect: Boolean? = null,
    val activeGrammarError: String? = null,
    val isLoading: Boolean = false,
    val isSessionFinished: Boolean = false,
    val sessionReviewedCount: Int = 0
)

sealed interface ReviewIntent {
    data class LoadQueue(val language: String) : ReviewIntent
    data class OnInputChanged(val input: String) : ReviewIntent
    data object SubmitAnswer : ReviewIntent
    data class ApplyRating(val rating: Rating) : ReviewIntent
    data object NextCard : ReviewIntent
    data object ResetError : ReviewIntent
}

class ReviewViewModel(
    private val repository: LanguageRepository? = null,
    private val getDailyQueueUseCase: GetDailyQueueUseCase? = null,
    private val processReviewUseCase: ProcessReviewUseCase? = null,
    private val evaluateReviewUseCase: EvaluateReviewUseCase = EvaluateReviewUseCase(repository)
) : ViewModel() {

    private val _state = MutableStateFlow(ReviewState())
    val state: StateFlow<ReviewState> = _state.asStateFlow()

    fun onIntent(intent: ReviewIntent) {
        when (intent) {
            is ReviewIntent.LoadQueue -> loadQueue(intent.language)
            is ReviewIntent.OnInputChanged -> handleInputChanged(intent.input)
            is ReviewIntent.SubmitAnswer -> submitAnswer()
            is ReviewIntent.ApplyRating -> rateCard(intent.rating)
            is ReviewIntent.NextCard -> advanceToNextCard()
            is ReviewIntent.ResetError -> _state.update { it.copy(activeGrammarError = null) }
        }
    }

    fun handleInputChanged(input: String) {
        _state.update {
            it.copy(
                userInput = input,
                activeGrammarError = null
            )
        }
    }

    fun loadQueue(language: String) {
        _state.update { it.copy(isLoading = true, language = language) }

        viewModelScope.launch {
            if (getDailyQueueUseCase != null) {
                getDailyQueueUseCase(language).collect { cards ->
                    val queue = if (cards.isEmpty()) getSampleQueue(language) else cards
                    _state.update {
                        it.copy(
                            queue = queue,
                            currentIndex = 0,
                            currentCard = queue.firstOrNull(),
                            isLoading = false,
                            isSessionFinished = queue.isEmpty()
                        )
                    }
                }
            } else {
                val sampleQueue = getSampleQueue(language)
                _state.update {
                    it.copy(
                        queue = sampleQueue,
                        currentIndex = 0,
                        currentCard = sampleQueue.firstOrNull(),
                        isLoading = false,
                        isSessionFinished = sampleQueue.isEmpty()
                    )
                }
            }
        }
    }

    fun submitAnswer() {
        val currentState = _state.value
        val card = currentState.currentCard ?: return
        if (currentState.isAnswerRevealed) return

        viewModelScope.launch {
            val evaluation = evaluateReviewUseCase(
                card = card,
                userInput = currentState.userInput
            )

            _state.update {
                it.copy(
                    isAnswerRevealed = true,
                    isCorrect = evaluation.isCorrect,
                    activeGrammarError = evaluation.activeGrammarError
                )
            }
        }
    }

    fun rateCard(rating: Rating) {
        val currentState = _state.value
        val card = currentState.currentCard ?: return

        viewModelScope.launch {
            processReviewUseCase?.invoke(card, rating)
            advanceToNextCard()
        }
    }

    private fun advanceToNextCard() {
        _state.update { current ->
            val nextIndex = current.currentIndex + 1
            if (nextIndex >= current.queue.size) {
                current.copy(
                    isSessionFinished = true,
                    sessionReviewedCount = current.sessionReviewedCount + 1,
                    currentCard = null,
                    isAnswerRevealed = false,
                    isCorrect = null,
                    activeGrammarError = null,
                    userInput = ""
                )
            } else {
                current.copy(
                    currentIndex = nextIndex,
                    currentCard = current.queue[nextIndex],
                    userInput = "",
                    isAnswerRevealed = false,
                    isCorrect = null,
                    activeGrammarError = null,
                    sessionReviewedCount = current.sessionReviewedCount + 1
                )
            }
        }
    }

    private fun getSampleQueue(language: String): List<ReviewCard> {
        val now = System.currentTimeMillis()
        return if (language == "de") {
            listOf(
                ReviewCard(
                    lexeme = Lexeme(
                        id = "de_warten",
                        rootWord = "warten auf",
                        language = "de",
                        wordType = "Verb",
                        grammarRequirements = "Präpositionalverb: warten auf + Akkusativ"
                    ),
                    srsState = SrsState(
                        wordId = "de_warten",
                        difficulty = 5.0,
                        stability = 3.0,
                        retrievability = 0.9,
                        lastReviewDate = now - 86400000L * 2,
                        nextReviewDate = now,
                        repetitionCount = 1,
                        skillType = "rektion"
                    )
                ),
                ReviewCard(
                    lexeme = Lexeme(
                        id = "de_entscheidung",
                        rootWord = "die Entscheidung",
                        language = "de",
                        wordType = "Noun",
                        grammarRequirements = "Femininum (die), Plural: die Entscheidungen"
                    ),
                    srsState = SrsState(
                        wordId = "de_entscheidung",
                        difficulty = 4.0,
                        stability = 5.0,
                        retrievability = 0.88,
                        lastReviewDate = now - 86400000L * 3,
                        nextReviewDate = now,
                        repetitionCount = 2,
                        skillType = "gender"
                    )
                ),
                ReviewCard(
                    lexeme = Lexeme(
                        id = "de_trotz",
                        rootWord = "trotz des Regens",
                        language = "de",
                        wordType = "Preposition",
                        grammarRequirements = "Präposition mit Genitiv: trotz + Genitiv"
                    ),
                    srsState = SrsState(
                        wordId = "de_trotz",
                        difficulty = 6.0,
                        stability = 2.0,
                        retrievability = 0.85,
                        lastReviewDate = now - 86400000L * 2,
                        nextReviewDate = now,
                        repetitionCount = 1,
                        skillType = "kasus"
                    )
                )
            )
        } else {
            listOf(
                ReviewCard(
                    lexeme = Lexeme(
                        id = "pl_robic",
                        rootWord = "zrobić",
                        language = "pl",
                        wordType = "Verb",
                        grammarRequirements = "Aspekt Dokonany (Perfektiv) pary: robić / zrobić"
                    ),
                    srsState = SrsState(
                        wordId = "pl_robic",
                        difficulty = 5.5,
                        stability = 3.0,
                        retrievability = 0.9,
                        lastReviewDate = now - 86400000L * 2,
                        nextReviewDate = now,
                        repetitionCount = 1,
                        skillType = "aspect"
                    )
                ),
                ReviewCard(
                    lexeme = Lexeme(
                        id = "pl_ksiazka",
                        rootWord = "książka",
                        language = "pl",
                        wordType = "Noun",
                        grammarRequirements = "Rzeczownik żeński (-a), Biernik: książkę"
                    ),
                    srsState = SrsState(
                        wordId = "pl_ksiazka",
                        difficulty = 4.2,
                        stability = 4.5,
                        retrievability = 0.89,
                        lastReviewDate = now - 86400000L * 4,
                        nextReviewDate = now,
                        repetitionCount = 2,
                        skillType = "gender"
                    )
                )
            )
        }
    }
}
