package com.ivy.transactions

import androidx.compose.runtime.Immutable
import com.ivy.data.model.Transaction
import com.ivy.data.model.TransactionHistoryItem
import com.ivy.data.model.Category
import com.ivy.data.model.Tag
import com.ivy.ui.period.TimePeriod
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import java.util.UUID

@Immutable
internal data class TransactionsState(
    val period: TimePeriod,
    val baseCurrency: String,
    val currency: String,
    val categories: ImmutableList<Category>,
    val accounts: ImmutableList<TransactionsListAccount>,
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
    val showAccountColorsInTransactions: Boolean,
    val accountFilter: AccountTransactionFilter?
)

@Immutable
internal data class AccountTransactionFilter(
    val availableCategories: ImmutableList<Category>,
    val hasUncategorized: Boolean,
    val availableTags: ImmutableList<Tag>,
    val selectedCategoryIds: ImmutableSet<UUID>,
    val uncategorizedSelected: Boolean,
    val selectedTagIds: ImmutableSet<UUID>,
) {
    val isActive: Boolean
        get() = selectedCategoryIds.isNotEmpty() ||
                uncategorizedSelected ||
                selectedTagIds.isNotEmpty()
}

@Immutable
internal data class TransactionsListAccount(
    val id: UUID,
    val name: String,
    val color: Int,
    val icon: String?,
    val currency: String?,
)

@Immutable
internal data class TransactionsDueSection(
    val transactions: ImmutableList<Transaction>,
    val expanded: Boolean,
    val income: Double,
    val expenses: Double,
)
