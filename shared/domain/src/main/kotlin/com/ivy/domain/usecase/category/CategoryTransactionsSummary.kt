package com.ivy.domain.usecase.category

import com.ivy.data.model.legacy.Transaction
import com.ivy.data.model.TransactionHistoryItem

data class CategoryTransactionsSummary(
    val balance: Double,
    val income: Double,
    val expenses: Double,
    val history: List<TransactionHistoryItem>,
    val upcoming: CategoryDueTransactionsSummary,
    val overdue: CategoryDueTransactionsSummary
)

data class CategoryDueTransactionsSummary(
    val income: Double,
    val expenses: Double,
    val transactions: List<Transaction>
)
