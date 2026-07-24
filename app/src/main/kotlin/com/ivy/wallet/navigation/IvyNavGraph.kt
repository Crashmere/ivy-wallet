package com.ivy.wallet.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import com.ivy.balance.BalanceScreen
import com.ivy.bulkedit.BulkEditScreen
import com.ivy.categories.CategoriesScreen
import com.ivy.exchangerates.ExchangeRatesScreen
import com.ivy.importdata.csv.CSVScreen as CSVImportScreen
import com.ivy.importdata.csvimport.ImportCSVScreen
import com.ivy.main.MainScreen
import com.ivy.ui.navigation.BalanceScreen
import com.ivy.ui.navigation.BulkEditScreen
import com.ivy.ui.navigation.CSVScreen
import com.ivy.ui.navigation.CategoriesScreen
import com.ivy.ui.navigation.EditTransactionScreen
import com.ivy.ui.navigation.ExchangeRatesScreen
import com.ivy.ui.navigation.ImportScreen
import com.ivy.ui.navigation.MainScreen
import com.ivy.ui.navigation.PieChartStatisticScreen
import com.ivy.ui.navigation.ReportScreen
import com.ivy.ui.navigation.Screen
import com.ivy.ui.navigation.SearchScreen
import com.ivy.ui.navigation.SettingsScreen
import com.ivy.ui.navigation.TransactionsScreen
import com.ivy.piechart.PieChartStatisticScreen
import com.ivy.reports.ReportScreen
import com.ivy.search.SearchScreen
import com.ivy.settings.SettingsScreen
import com.ivy.transaction.EditTransactionScreen
import com.ivy.transactions.TransactionsScreen

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
@Suppress("CyclomaticComplexMethod", "FunctionNaming")
internal fun BoxWithConstraintsScope.IvyNavGraph(screen: Screen?) {
    when (screen) {
        null -> {
            // show nothing
        }

        is MainScreen -> MainScreen()
        is ExchangeRatesScreen -> ExchangeRatesScreen()
        is EditTransactionScreen -> EditTransactionScreen(screen = screen)
        is TransactionsScreen -> TransactionsScreen(screen = screen)
        is PieChartStatisticScreen -> PieChartStatisticScreen(screen = screen)
        is CategoriesScreen -> CategoriesScreen()
        is SettingsScreen -> SettingsScreen()
        is BalanceScreen -> BalanceScreen()
        ImportScreen -> ImportCSVScreen()
        is ReportScreen -> ReportScreen()
        is SearchScreen -> SearchScreen()
        is BulkEditScreen -> BulkEditScreen()
        CSVScreen -> CSVImportScreen()
    }
}
