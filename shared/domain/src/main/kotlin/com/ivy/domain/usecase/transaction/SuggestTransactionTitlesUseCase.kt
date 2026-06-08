package com.ivy.domain.usecase.transaction

import com.ivy.data.api.TransactionStore
import com.ivy.data.model.AccountId
import com.ivy.data.model.CategoryId
import com.ivy.data.model.Transaction
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

private const val SUGGESTIONS_LIMIT = 10

class SuggestTransactionTitlesUseCase @Inject internal constructor(
    private val transactionStore: TransactionStore
) {
    suspend operator fun invoke(
        title: String?,
        categoryId: UUID?,
        accountId: UUID?
    ): Set<String> {
        val suggestions = mutableSetOf<String>()

        if (title != null && title.isNotEmpty()) {
            val suggestionsByTitle = transactionStore.findAllByTitleMatchingPattern("$title%")
                .extractUniqueTitles()
                .sortedByMostUsedFirst {
                    transactionStore.countByTitleMatchingPattern("$it%").value
                }

            suggestions.addAll(suggestionsByTitle)
        }

        if (categoryId != null) {
            val category = CategoryId(categoryId)
            val suggestionsByCategory = transactionStore
                .findAllByCategory(categoryId = category)
                .extractUniqueTitles(excludeSuggestions = suggestions)
                .sortedByMostUsedFirst {
                    transactionStore.countByTitleMatchingPatternAndCategory(
                        pattern = it,
                        categoryId = category
                    ).value
                }

            suggestions.addAll(suggestionsByCategory)
        }

        if (suggestions.size < SUGGESTIONS_LIMIT && accountId != null) {
            val account = AccountId(accountId)
            val suggestionsByAccount = transactionStore
                .findAllByAccount(accountId = account)
                .extractUniqueTitles(excludeSuggestions = suggestions)
                .sortedByMostUsedFirst {
                    transactionStore.countByTitleMatchingPatternAndAccount(
                        pattern = it,
                        accountId = account
                    ).value
                }

            suggestions.addAll(suggestionsByAccount)
        }

        return suggestions
            .filter { it != title }
            .toSet()
    }
}

private fun List<Transaction>.extractUniqueTitles(
    excludeSuggestions: Set<String>? = null
): Set<String> =
    filter { it.title != null }
        .map { it.title!!.value.trim().capitalizeWords() }
        .filter { excludeSuggestions == null || !excludeSuggestions.contains(it) }
        .toSet()

private suspend fun Set<String>.sortedByMostUsedFirst(
    countUses: suspend (String) -> Long
): Set<String> {
    val titleCountMap = associateWith { countUses(it) }
    return sortedByDescending { titleCountMap.getOrDefault(it, 0) }.toSet()
}

private fun String.capitalizeWords(): String =
    split(" ").joinToString(" ") { it.capitalizeLocal() }

private fun String.capitalizeLocal(): String = replaceFirstChar {
    if (it.isLowerCase()) {
        it.titlecase(Locale.getDefault())
    } else {
        it.toString()
    }
}
