package com.ivy.main

import com.ivy.ui.compose.BackPressHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.ivy.ui.navigation.screenScopedViewModel
import com.ivy.accounts.AccountsTab
import com.ivy.home.HomeTab
import com.ivy.ui.navigation.onScreenStart
import com.ivy.ui.navigation.EditPlannedScreen
import com.ivy.ui.navigation.EditTransactionScreen
import com.ivy.ui.navigation.ReportScreen
import com.ivy.ui.navigation.SettingsScreen
import com.ivy.ui.navigation.TransactionRouteType
import com.ivy.ui.navigation.navigation
import com.ivy.data.model.CreateAccountData
import com.ivy.accounts.AccountCreationModal

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun BoxWithConstraintsScope.MainScreen() {
    val viewModel: MainViewModel = screenScopedViewModel()

    val currency by viewModel.currency.collectAsState()

    onScreenStart {
        viewModel.start()
    }

    BackPressHandler(enabled = viewModel.selectedTab == MainTab.ACCOUNTS) {
        viewModel.selectTab(MainTab.HOME)
    }

    UI(
        tab = viewModel.selectedTab,
        baseCurrency = currency,
        selectTab = viewModel::selectTab,
        onCreateAccount = viewModel::createAccount
    )
}

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
private fun BoxWithConstraintsScope.UI(
    tab: MainTab,

    baseCurrency: String,

    selectTab: (MainTab) -> Unit,
    onCreateAccount: (CreateAccountData) -> Unit,
) {
    var accountModalVisible by remember { mutableStateOf(false) }

    when (tab) {
        MainTab.HOME -> HomeTab(
            onOpenAccountsTab = {
                selectTab(MainTab.ACCOUNTS)
            }
        )

        MainTab.ACCOUNTS -> AccountsTab(
            onOpenHomeTab = {
                selectTab(MainTab.HOME)
            },
            onAddAccount = {
                accountModalVisible = true
            }
        )
    }

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

        onOpenReports = {
            nav.navigateTo(ReportScreen)
        },
        onOpenProfile = {
            nav.navigateTo(SettingsScreen)
        },

        showAddAccountModal = {
            accountModalVisible = true
        }
    )

    AccountCreationModal(
        visible = accountModalVisible,
        baseCurrency = baseCurrency,
        onCreateAccount = onCreateAccount,
        dismiss = {
            accountModalVisible = false
        }
    )
}
