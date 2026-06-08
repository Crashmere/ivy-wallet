package com.ivy.transactions

import androidx.compose.runtime.Immutable
import com.ivy.data.model.legacy.LegacyTransaction
import com.ivy.data.model.TransactionHistoryItem
import com.ivy.data.model.Category
import com.ivy.ui.period.TimePeriod
import com.ivy.legacy.ui.transaction.TransactionListAccount
import kotlinx.collections.immutable.ImmutableList

@Immutable
internal data class TransactionsState(
    val period: TimePeriod,
    val baseCurrency: String,
    val currency: String,
    val categories: ImmutableList<Category>,
    val accounts: ImmutableList<TransactionListAccount>,
    val account: TransactionsAccount?,
    val category: Category?,
    val balance: Double,
    val balanceBaseCurrency: Double?,
    val income: Double,
    val expenses: Double,
    val incomeTransactionCount: Int,
    val expenseTransactionCount: Int,
    val initWithTransactions: Boolean,
    val treatTransfersAsIncomeExpense: Boolean,
    val history: ImmutableList<TransactionHistoryItem>,
    val upcoming: TransactionsDueSection,
    val overdue: TransactionsDueSection,
    val enableDeletionButton: Boolean,
    val skipAllModalVisible: Boolean,
    val deleteModal1Visible: Boolean,
    val showAccountColorsInTransactions: Boolean
)

@Immutable
internal data class TransactionsDueSection(
    val transactions: ImmutableList<LegacyTransaction>,
    val expanded: Boolean,
    val income: Double,
    val expenses: Double,
)
