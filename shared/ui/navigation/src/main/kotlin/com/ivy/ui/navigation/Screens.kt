package com.ivy.ui.navigation

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.util.UUID

data object MainScreen : Screen

data object CSVScreen : Screen

data class EditTransactionScreen(
    val initialTransactionId: UUID?,
    val type: TransactionRouteType,
    // extras
    val accountId: UUID? = null,
    val categoryId: UUID? = null
) : Screen

data class TransactionsScreen(
    val accountId: UUID? = null,
    val categoryId: UUID? = null,
    val unspecifiedCategory: Boolean? = false,
    val accountIdFilterList: List<UUID> = persistentListOf(),
    val legacyTransactionIds: List<UUID> = persistentListOf(),
    val containsTransferTransactions: Boolean = false
) : Screen

data class PieChartStatisticScreen(
    val type: TransactionRouteType,
    val filterExcluded: Boolean = true,
    val accountList: ImmutableList<UUID> = persistentListOf(),
    val legacyTransactionIds: ImmutableList<UUID> = persistentListOf(),
    val treatTransfersAsIncomeExpense: Boolean = false
) : Screen

data class EditPlannedScreen(
    val plannedPaymentRuleId: UUID?,
    val type: TransactionRouteType,
    val amount: Double? = null,
    val accountId: UUID? = null,
    val categoryId: UUID? = null,
    val title: String? = null,
    val description: String? = null,
) : Screen {
    fun mandatoryFilled(): Boolean {
        return amount != null && amount > 0.0 &&
                accountId != null
    }
}

data object BalanceScreen : Screen

data object PlannedPaymentsScreen : Screen

data object CategoriesScreen : Screen

data object SettingsScreen : Screen

data object ImportScreen : Screen

data object ReportScreen : Screen

data object BudgetScreen : Screen

data object LoansScreen : Screen

data object SearchScreen : Screen

data class LoanDetailsScreen(
    val loanId: UUID
) : Screen

data object ExchangeRatesScreen : Screen
