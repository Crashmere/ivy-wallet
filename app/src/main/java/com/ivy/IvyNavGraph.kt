package com.ivy

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import com.ivy.balance.BalanceScreen
import com.ivy.budgets.BudgetScreen
import com.ivy.categories.CategoriesScreen
import com.ivy.exchangerates.ExchangeRatesScreen
import com.ivy.importdata.csv.CSVScreen as CSVImportScreen
import com.ivy.importdata.csvimport.ImportCSVScreen
import com.ivy.loans.loan.LoansScreen
import com.ivy.loans.loandetails.LoanDetailsScreen
import com.ivy.main.MainScreen
import com.ivy.ui.navigation.BalanceScreen
import com.ivy.ui.navigation.BudgetScreen
import com.ivy.ui.navigation.CSVScreen
import com.ivy.ui.navigation.CategoriesScreen
import com.ivy.ui.navigation.EditPlannedScreen
import com.ivy.ui.navigation.EditTransactionScreen
import com.ivy.ui.navigation.ExchangeRatesScreen
import com.ivy.ui.navigation.ImportScreen
import com.ivy.ui.navigation.LoanDetailsScreen
import com.ivy.ui.navigation.LoansScreen
import com.ivy.ui.navigation.MainScreen
import com.ivy.ui.navigation.PieChartStatisticScreen
import com.ivy.ui.navigation.PlannedPaymentsScreen
import com.ivy.ui.navigation.ReportScreen
import com.ivy.ui.navigation.Screen
import com.ivy.ui.navigation.SearchScreen
import com.ivy.ui.navigation.SettingsScreen
import com.ivy.ui.navigation.TransactionsScreen
import com.ivy.piechart.PieChartStatisticScreen
import com.ivy.planned.edit.EditPlannedScreen
import com.ivy.planned.list.PlannedPaymentsScreen
import com.ivy.reports.ReportScreen
import com.ivy.search.SearchScreen
import com.ivy.settings.SettingsScreen
import com.ivy.transaction.EditTransactionScreen
import com.ivy.transactions.TransactionsScreen

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
@Suppress("CyclomaticComplexMethod", "FunctionNaming")
fun BoxWithConstraintsScope.IvyNavGraph(screen: Screen?) {
    when (screen) {
        null -> {
            // show nothing
        }

        is MainScreen -> MainScreen(screen = screen)
        is ExchangeRatesScreen -> ExchangeRatesScreen()
        is EditTransactionScreen -> EditTransactionScreen(screen = screen)
        is TransactionsScreen -> TransactionsScreen(screen = screen)
        is PieChartStatisticScreen -> PieChartStatisticScreen(screen = screen)
        is CategoriesScreen -> CategoriesScreen(screen = screen)
        is SettingsScreen -> SettingsScreen()
        is PlannedPaymentsScreen -> PlannedPaymentsScreen(screen = screen)
        is EditPlannedScreen -> EditPlannedScreen(screen = screen)
        is BalanceScreen -> BalanceScreen(screen = screen)
        ImportScreen -> ImportCSVScreen()
        is ReportScreen -> ReportScreen(screen = screen)
        is BudgetScreen -> BudgetScreen(screen = screen)
        is LoansScreen -> LoansScreen(screen = screen)
        is LoanDetailsScreen -> LoanDetailsScreen(screen = screen)
        is SearchScreen -> SearchScreen(screen = screen)
        CSVScreen -> CSVImportScreen()
    }
}
