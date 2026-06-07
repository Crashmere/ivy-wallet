package com.ivy.domain.usecase.transaction

import com.ivy.base.model.legacy.Transaction
import com.ivy.base.text.capitalizeWords
import com.ivy.base.text.isNotNullOrBlank
import com.ivy.data.db.dao.read.TransactionDao
import com.ivy.domain.mapper.legacy.toLegacyDomain
import java.util.UUID
import javax.inject.Inject

private const val SUGGESTIONS_LIMIT = 10

class SuggestTransactionTitlesUseCase @Inject constructor(
    private val transactionDao: TransactionDao
) {
    suspend operator fun invoke(
        title: String?,
        categoryId: UUID?,
        accountId: UUID?
    ): Set<String> {
        val suggestions = mutableSetOf<String>()

        if (title != null && title.isNotEmpty()) {
            val suggestionsByTitle = transactionDao.findAllByTitleMatchingPattern("$title%")
                .map { it.toLegacyDomain() }
                .extractUniqueTitles()
                .sortedByMostUsedFirst {
                    transactionDao.countByTitleMatchingPattern("$it%")
                }

            suggestions.addAll(suggestionsByTitle)
        }

        if (categoryId != null) {
            val suggestionsByCategory = transactionDao
                .findAllByCategory(categoryId = categoryId)
                .map { it.toLegacyDomain() }
                .extractUniqueTitles(excludeSuggestions = suggestions)
                .sortedByMostUsedFirst {
                    transactionDao.countByTitleMatchingPatternAndCategoryId(
                        pattern = it,
                        categoryId = categoryId
                    )
                }

            suggestions.addAll(suggestionsByCategory)
        }

        if (suggestions.size < SUGGESTIONS_LIMIT && accountId != null) {
            val suggestionsByAccount = transactionDao
                .findAllByAccount(accountId = accountId)
                .map { it.toLegacyDomain() }
                .extractUniqueTitles(excludeSuggestions = suggestions)
                .sortedByMostUsedFirst {
                    transactionDao.countByTitleMatchingPatternAndAccountId(
                        pattern = it,
                        accountId = accountId
                    )
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
    filter { it.title.isNotNullOrBlank() }
        .map { it.title!!.trim().capitalizeWords() }
        .filter { excludeSuggestions == null || !excludeSuggestions.contains(it) }
        .toSet()

private suspend fun Set<String>.sortedByMostUsedFirst(
    countUses: suspend (String) -> Long
): Set<String> {
    val titleCountMap = associateWith { countUses(it) }
    return sortedByDescending { titleCountMap.getOrDefault(it, 0) }.toSet()
}
