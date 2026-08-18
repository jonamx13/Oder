package com.oder.domain.usecase

import com.oder.core.fsrs.Rating
import com.oder.domain.model.ReviewCard
import com.oder.domain.repository.LanguageRepository

data class EvaluationResult(
    val isCorrect: Boolean,
    val activeGrammarError: String?,
    val suggestedRating: Rating
)

class EvaluateReviewUseCase(
    private val repository: LanguageRepository? = null
) {
    suspend operator fun invoke(
        card: ReviewCard,
        userInput: String
    ): EvaluationResult {
        val normalizedInput = userInput.trim()
        val normalizedTarget = card.lexeme.rootWord.trim()

        val isExactMatch = normalizedInput.equals(normalizedTarget, ignoreCase = true)

        if (isExactMatch) {
            return EvaluationResult(
                isCorrect = true,
                activeGrammarError = null,
                suggestedRating = Rating.GOOD
            )
        }

        // Evaluate grammar interceptors on failure
        var grammarError: String? = null

        // 1. Query registered Grammar Rules for this target word
        val matchingRules = repository?.getGrammarRulesForTargetWord(normalizedTarget) ?: emptyList()
        if (matchingRules.isNotEmpty()) {
            val triggeredRule = matchingRules.firstOrNull { rule ->
                normalizedInput.contains(rule.conditionCheck, ignoreCase = true) ||
                    !normalizedInput.contains(rule.conditionCheck, ignoreCase = true)
            } ?: matchingRules.first()
            grammarError = triggeredRule.errorMessage
        }

        // 2. Fallback to parsing Lexeme's grammarRequirements string
        if (grammarError == null && card.lexeme.grammarRequirements.isNotBlank()) {
            val req = card.lexeme.grammarRequirements
            grammarError = when {
                req.contains("Akkusativ", ignoreCase = true) ->
                    "Grammar Interceptor: Akkusativ required (${card.lexeme.grammarRequirements}). Check case inflection."
                req.contains("Dativ", ignoreCase = true) ->
                    "Grammar Interceptor: Dativ required (${card.lexeme.grammarRequirements}). Check article/ending."
                req.contains("Genitiv", ignoreCase = true) ->
                    "Grammar Interceptor: Genitiv required (${card.lexeme.grammarRequirements}). Check noun suffix."
                req.contains("Aspekt", ignoreCase = true) ->
                    "Grammar Interceptor: Aspekt error (${card.lexeme.grammarRequirements}). Use the required aspect pair."
                req.contains("Narzędnik", ignoreCase = true) ->
                    "Grammar Interceptor: Narzędnik required (${card.lexeme.grammarRequirements})."
                req.contains("Miejscownik", ignoreCase = true) ->
                    "Grammar Interceptor: Miejscownik required (${card.lexeme.grammarRequirements})."
                else ->
                    "Grammar Requirement: ${card.lexeme.grammarRequirements}. Target: '$normalizedTarget'"
            }
        }

        if (grammarError == null) {
            grammarError = "Linguistic Mismatch: Expected '$normalizedTarget'"
        }

        return EvaluationResult(
            isCorrect = false,
            activeGrammarError = grammarError,
            suggestedRating = Rating.HARD
        )
    }
}
