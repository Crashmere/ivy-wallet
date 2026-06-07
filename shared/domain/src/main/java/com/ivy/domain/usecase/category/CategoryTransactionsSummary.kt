package com.ivy.domain.usecase.category

import com.ivy.base.model.legacy.Transaction
import com.ivy.base.model.legacy.TransactionHistoryItem

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
