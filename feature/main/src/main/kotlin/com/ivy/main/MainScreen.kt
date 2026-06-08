package com.ivy.main

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ivy.accounts.AccountsTab
import com.ivy.home.HomeTab
import com.ivy.ui.navigation.onScreenStart
import com.ivy.ui.navigation.EditPlannedScreen
import com.ivy.ui.navigation.EditTransactionScreen
import com.ivy.ui.main.LocalMainTabState
import com.ivy.ui.main.MainTab
import com.ivy.ui.navigation.MainScreen
import com.ivy.ui.navigation.TransactionRouteType
import com.ivy.ui.navigation.navigation
import com.ivy.data.model.CreateAccountData
import com.ivy.legacy.ui.modal.edit.AccountModal
import com.ivy.legacy.ui.modal.AccountModalData

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun BoxWithConstraintsScope.MainScreen(screen: MainScreen) {
    val viewModel: MainViewModel = viewModel()

    val currency by viewModel.currency.collectAsState()

    onScreenStart {
        viewModel.start(screen)
    }

    CompositionLocalProvider(LocalMainTabState provides viewModel.mainTabState) {
        UI(
            screen = screen,
            tab = viewModel.selectedTab(),
            baseCurrency = currency,
            selectTab = viewModel::selectTab,
            onCreateAccount = viewModel::createAccount
        )
    }
}

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
private fun BoxWithConstraintsScope.UI(
    screen: MainScreen,
    tab: MainTab,

    baseCurrency: String,

    selectTab: (MainTab) -> Unit,
    onCreateAccount: (CreateAccountData) -> Unit,
) {
    when (tab) {
        MainTab.HOME -> HomeTab()
        MainTab.ACCOUNTS -> AccountsTab()
    }

    var accountModalData: AccountModalData? by remember { mutableStateOf(null) }

    val nav = navigation()
    BottomBar(
        tab = tab,
        selectTab = selectTab,

        onAddIncome = {
            nav.navigateTo(
                EditTransactionScreen(
                    initialTransactionId = null,
                    type = TransactionRouteType.INCOME
                )
            )
        },
        onAddExpense = {
            nav.navigateTo(
                EditTransactionScreen(
                    initialTransactionId = null,
                    type = TransactionRouteType.EXPENSE
                )
            )
        },
        onAddTransfer = {
            nav.navigateTo(
                EditTransactionScreen(
                    initialTransactionId = null,
                    type = TransactionRouteType.TRANSFER
                )
            )
        },
        onAddPlannedPayment = {
            nav.navigateTo(
                EditPlannedScreen(
                    type = TransactionRouteType.EXPENSE,
                    plannedPaymentRuleId = null
                )
            )
        },

        showAddAccountModal = {
            accountModalData = AccountModalData(
                account = null,
                balance = 0.0,
                baseCurrency = baseCurrency
            )
        }
    )

    AccountModal(
        modal = accountModalData,
        onCreateAccount = onCreateAccount,
        onEditAccount = { _, _ -> },
        dismiss = {
            accountModalData = null
        }
    )
}
