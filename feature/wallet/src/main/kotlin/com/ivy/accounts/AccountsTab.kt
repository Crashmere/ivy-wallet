package com.ivy.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.ui.compose.clickableNoIndication
import com.ivy.ui.compose.horizontalSwipeListener
import com.ivy.ui.compose.rememberInteractionSource
import com.ivy.ui.compose.rememberSwipeListenerState
import com.ivy.ui.navigation.TransactionsScreen
import com.ivy.ui.navigation.navigation
import com.ivy.ui.navigation.screenScopedViewModel
import com.ivy.ui.R
import com.ivy.ui.rememberScrollPositionListState
import com.ivy.ui.money.BalanceRow
import com.ivy.ui.icon.ItemIconSDefaultIcon
import com.ivy.ui.modal.ReorderModalSingleType
import com.ivy.ui.compose.FilledIconButton
import com.ivy.ui.theme.colors.dynamicContrast
import com.ivy.ui.theme.colors.findContrastTextColor
import com.ivy.ui.theme.colors.toComposeColor

@Composable
fun BoxWithConstraintsScope.AccountsTab(
    onOpenHomeTab: () -> Unit,
) {
    val viewModel: AccountsViewModel = screenScopedViewModel()
    val uiState = viewModel.uiState()

    UI(
        state = uiState,
        onEvent = viewModel::onEvent,
        onOpenHomeTab = onOpenHomeTab,
    )
}

@Composable
private fun BoxWithConstraintsScope.UI(
    state: AccountsState,
    onEvent: (AccountsEvent) -> Unit = {},
    onOpenHomeTab: () -> Unit,
) {
    val nav = navigation()
    val listState = rememberScrollPositionListState(
        key = "accounts_lazy_column"
    )
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .horizontalSwipeListener(
                sensitivity = 200,
                state = rememberSwipeListenerState(),
                onSwipeLeft = {
                    onOpenHomeTab()
                },
                onSwipeRight = {
                    onOpenHomeTab()
                }
            ),
        state = listState
    ) {
        item {
            Spacer(Modifier.height(32.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.width(24.dp))

                Column {
                    Text(
                        text = stringResource(R.string.accounts),
                        style = AccountsTheme.typo.b1.copy(
                            color = AccountsTheme.colors.pureInverse,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Start
                        )
                    )
                }

                Spacer(Modifier.weight(1f))

                FilledIconButton(
                    icon = R.drawable.ic_drag_handle,
                    contentDescription = "reorder",
                    backgroundColor = AccountsTheme.colors.medium,
                    tint = AccountsTheme.colors.pureInverse,
                ) {
                    onEvent(
                        AccountsEvent.OnReorderModalVisible(reorderVisible = true)
                    )
                }

                Spacer(Modifier.width(24.dp))
            }
            if (!state.hideTotalBalance) {
                Column {
                    Spacer(Modifier.height(16.dp))
                    IncomeExpensesRow(
                        currency = state.baseCurrency,
                        incomeLabel = stringResource(id = R.string.total_balance),
                        income = state.totalBalanceWithoutExcluded.toDoubleOrNull() ?: 0.00,
                        expensesLabel = stringResource(id = R.string.total_balance_excluded),
                        expenses = state.totalBalanceWithExcluded.toDoubleOrNull() ?: 0.00
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
        items(state.accountsData) {
            Spacer(Modifier.height(16.dp))
            AccountCard(
                baseCurrency = state.baseCurrency,
                accountData = it,
                compactModeEnabled = state.compactAccountsModeEnabled,
                onBalanceClick = {
                    nav.navigateTo(
                        TransactionsScreen(
                            accountId = it.account.id.value,
                            categoryId = null
                        )
                    )
                }
            ) {
                nav.navigateTo(
                    TransactionsScreen(
                        accountId = it.account.id.value,
                        categoryId = null
                    )
                )
            }
        }

        item {
            Spacer(Modifier.height(150.dp)) // scroll hack
        }
    }

    ReorderModalSingleType(
        visible = state.reorderVisible,
        initialItems = state.accountsData,
        itemOrderNum = { it.account.orderNum },
        withNewOrderNum = { item, newOrderNum ->
            item.copy(
                account = item.account.copy(
                    orderNum = newOrderNum
                )
            )
        },
        dismiss = {
            onEvent(AccountsEvent.OnReorderModalVisible(reorderVisible = false))
        },
        onReordered = {
            onEvent(AccountsEvent.OnReorder(accountIds = it.map { item -> item.account.id }))
        }
    ) { _, item ->
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 24.dp)
                .padding(vertical = 8.dp),
            text = item.account.name.value,
            style = AccountsTheme.typo.b1.copy(
                color = item.account.color.value.toComposeColor(),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start
            )
        )
    }
}

@Composable
private fun AccountCard(
    baseCurrency: String,
    accountData: AccountData,
    compactModeEnabled: Boolean,
    onBalanceClick: () -> Unit,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(AccountsTheme.shapes.r4)
            .border(2.dp, AccountsTheme.colors.medium, AccountsTheme.shapes.r4)
            .clickable(
                onClick = onClick
            )
    ) {
        val account = accountData.account
        val contrastColor = findContrastTextColor(account.color.value.toComposeColor())
        val currency = account.asset.code

        AccountHeader(
            accountData = accountData,
            currency = currency,
            baseCurrency = baseCurrency,
            contrastColor = contrastColor,
            onBalanceClick = onBalanceClick
        )

        if (!compactModeEnabled) {
            Spacer(Modifier.height(12.dp))

            IncomeExpensesRow(
                currency = currency,
                incomeLabel = stringResource(R.string.month_income),
                income = accountData.monthlyIncome,
                expensesLabel = stringResource(R.string.month_expenses),
                expenses = accountData.monthlyExpenses
            )

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun AccountHeader(
    accountData: AccountData,
    currency: String,
    baseCurrency: String,
    contrastColor: Color,
    onBalanceClick: () -> Unit
) {
    val account = accountData.account

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(account.color.value.toComposeColor(), AccountsTheme.shapes.r4Top)
    ) {
        Spacer(Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(20.dp))

            ItemIconSDefaultIcon(
                iconName = account.icon?.id,
                defaultIcon = R.drawable.ic_custom_account_s,
                tint = contrastColor
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = account.name.value,
                style = AccountsTheme.typo.b1.copy(
                    color = contrastColor,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Start
                )
            )

            if (!account.includeInBalance) {
                Spacer(Modifier.width(8.dp))

                Text(
                    text = stringResource(R.string.excluded),
                    style = AccountsTheme.typo.c.copy(
                        color = account.color.value.toComposeColor().dynamicContrast(),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start
                    )
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        BalanceRow(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickableNoIndication(rememberInteractionSource()) {
                    onBalanceClick()
                },
            textColor = contrastColor,
            currency = currency,
            balance = accountData.balance,

            balanceFontSize = 30.sp,
            currencyFontSize = 30.sp,

            currencyUpfront = false
        )

        if (currency != baseCurrency && accountData.balanceBaseCurrency != null) {
            BalanceRow(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickableNoIndication(rememberInteractionSource()) {
                        onBalanceClick()
                    }
                    .testTag("baseCurrencyEquivalent"),
                textColor = account.color.value.toComposeColor().dynamicContrast(),
                currency = baseCurrency,
                balance = accountData.balanceBaseCurrency!!,
                spacerCurrency = 8.dp,
                currencyFontSize = 20.sp,
                balanceFontSize = 22.sp,
                currencyUpfront = false
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}
