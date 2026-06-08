package com.ivy.reports

import androidx.compose.runtime.Immutable
import com.ivy.data.model.TransactionHistoryItem
import com.ivy.data.model.Category
import com.ivy.data.model.Tag
import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.data.model.legacy.LegacyTransaction
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.util.UUID

@Suppress("DataClassDefaultValues")
internal data class ReportScreenState(
    val baseCurrency: String = "",
    val balance: Double = 0.0,
    val income: Double = 0.0,
    val expenses: Double = 0.0,
    val history: ImmutableList<TransactionHistoryItem> = persistentListOf(),
    val upcoming: ReportDueSection = ReportDueSection(
        transactions = persistentListOf(),
        expanded = false,
        income = 0.0,
        expenses = 0.0,
    ),
    val overdue: ReportDueSection = ReportDueSection(
        transactions = persistentListOf(),
        expanded = false,
        income = 0.0,
        expenses = 0.0,
    ),
    val categories: ImmutableList<Category> = persistentListOf(),
    val accounts: ImmutableList<LegacyAccount> = persistentListOf(),
    val filter: ReportFilter? = null,
    val loading: Boolean = false,
    val accountIdFilters: ImmutableList<UUID> = persistentListOf(),
    val transactionSummary: ReportTransactionSummary = ReportTransactionSummary(),
    val filterOverlayVisible: Boolean = false,
    val showTransfersAsIncExpCheckbox: Boolean = false,
    val treatTransfersAsIncExp: Boolean = false,
    val allTags: ImmutableList<Tag> = persistentListOf(),
    val showAccountColorsInTransactions: Boolean = false
)

@Immutable
internal data class ReportDueSection(
    val transactions: ImmutableList<LegacyTransaction>,
    val expanded: Boolean,
    val income: Double,
    val expenses: Double,
)

@Immutable
internal data class ReportTransactionSummary(
    val transactionIds: ImmutableList<UUID> = persistentListOf(),
    val incomeTransactionCount: Int = 0,
    val expenseTransactionCount: Int = 0,
) {
    val hasTransactions: Boolean
        get() = transactionIds.isNotEmpty()
}
