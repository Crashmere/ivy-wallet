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
    val unspecifiedCategory: Boolean = false,
    val accountIdFilterList: ImmutableList<UUID> = persistentListOf(),
    val transactionIds: ImmutableList<UUID> = persistentListOf(),
    val containsTransferTransactions: Boolean = false
) : Screen

data class PieChartStatisticScreen(
    val type: TransactionRouteType,
    val filterExcluded: Boolean = true,
    val accountIdFilterList: ImmutableList<UUID> = persistentListOf(),
    val transactionIds: ImmutableList<UUID> = persistentListOf(),
    val treatTransfersAsIncomeExpense: Boolean = false
) : Screen

data object BalanceScreen : Screen

data object CategoriesScreen : Screen

data object SettingsScreen : Screen

data object ImportScreen : Screen

data object ReportScreen : Screen

data object SearchScreen : Screen

data object BulkEditScreen : Screen

data object ExchangeRatesScreen : Screen
