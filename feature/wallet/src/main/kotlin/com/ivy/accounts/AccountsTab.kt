package com.ivy.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.data.model.currency.format
import com.ivy.data.model.currency.shortenAmount
import com.ivy.data.model.currency.shouldShortAmount
import com.ivy.ui.compose.ResourceIcon
import com.ivy.ui.compose.horizontalSwipeListener
import com.ivy.ui.compose.rememberSwipeListenerState
import com.ivy.ui.navigation.TransactionsScreen
import com.ivy.ui.navigation.navigation
import com.ivy.ui.navigation.screenScopedViewModel
import com.ivy.ui.R
import com.ivy.ui.rememberScrollPositionListState
import com.ivy.ui.money.BalanceRow
import com.ivy.ui.icon.ItemIconSDefaultIcon
import com.ivy.ui.modal.ReorderModalSingleType
import com.ivy.ui.theme.colors.IvyGradients
import com.ivy.ui.theme.colors.IvyFixedColors.Green
import com.ivy.ui.theme.colors.IvyFixedColors.Red
import com.ivy.ui.theme.colors.IvyFixedColors.White
import com.ivy.ui.theme.colors.toComposeColor
import kotlin.math.absoluteValue

@Composable
fun BoxWithConstraintsScope.AccountsTab(
    onOpenHomeTab: () -> Unit,
    onAddAccount: () -> Unit,
) {
    val viewModel: AccountsViewModel = screenScopedViewModel()
    val uiState = viewModel.uiState()

    UI(
        state = uiState,
        onEvent = viewModel::onEvent,
        onOpenHomeTab = onOpenHomeTab,
        onAddAccount = onAddAccount,
    )
}

@Composable
private fun BoxWithConstraintsScope.UI(
    state: AccountsState,
    onEvent: (AccountsEvent) -> Unit = {},
    onOpenHomeTab: () -> Unit,
    onAddAccount: () -> Unit = {},
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
            Spacer(Modifier.height(20.dp))

            AccountsHeaderRow(
                onEdit = {
                    onEvent(AccountsEvent.OnReorderModalVisible(reorderVisible = true))
                },
            )

            Spacer(Modifier.height(16.dp))

            NetWorthCard(
                currency = state.baseCurrency,
                netWorth = state.netWorth,
                change = state.netWorthChange,
                hideBalance = state.hideTotalBalance,
            )

            Spacer(Modifier.height(6.dp))
        }

        items(state.accountsData) {
            Spacer(Modifier.height(10.dp))
            AccountCard(
                baseCurrency = state.baseCurrency,
                accountData = it,
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
            Spacer(Modifier.height(16.dp))
            DashedAddAccountButton(onClick = onAddAccount)
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
private fun AccountsHeaderRow(
    onEdit: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.accounts),
            style = AccountsTheme.typo.b1.copy(
                color = AccountsTheme.colors.pureInverse,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Start,
                fontSize = 24.sp,
            ),
        )

        Spacer(Modifier.weight(1f))

        Text(
            modifier = Modifier
                .clip(AccountsTheme.shapes.rFull)
                .clickable(onClick = onEdit)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            text = stringResource(R.string.edit),
            style = AccountsTheme.typo.b2.copy(
                color = Green,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
            ),
        )
    }
}

@Composable
private fun NetWorthCard(
    currency: String,
    netWorth: Double,
    change: Double,
    hideBalance: Boolean,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(AccountsTheme.shapes.r4)
            .background(IvyGradients.Dark.asHorizontalBrush())
            .padding(20.dp),
    ) {
        Text(
            text = stringResource(R.string.net_worth),
            style = AccountsTheme.typo.c.copy(
                color = White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
            ),
        )

        Spacer(Modifier.height(10.dp))

        BalanceRow(
            currency = currency,
            balance = netWorth,
            textColor = White,
            hiddenMode = hideBalance,
            balanceFontSize = 32.sp,
            shortenBigNumbers = true,
        )

        if (!hideBalance && change.absoluteValue >= 0.005) {
            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.vs_last_month),
                    style = AccountsTheme.typo.c.copy(
                        color = White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Start,
                    ),
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "${if (change >= 0) "▲" else "▼"} ${change.absoluteValue.format(currency)}",
                    style = AccountsTheme.typo.c.copy(
                        color = if (change >= 0) Green else Red,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                    ),
                )
            }
        }
    }
}

@Composable
private fun AccountCard(
    baseCurrency: String,
    accountData: AccountData,
    onClick: () -> Unit
) {
    val account = accountData.account
    val accountColor = account.color.value.toComposeColor()
    val currency = account.asset.code
    val secondaryColor = AccountsTheme.colors.pureInverse.copy(alpha = 0.5f)

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(AccountsTheme.shapes.r4)
            .background(AccountsTheme.colors.pure)
            .border(1.dp, AccountsTheme.colors.medium, AccountsTheme.shapes.r4)
            .clickable(onClick = onClick)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(5.dp)
                .fillMaxHeight()
                .background(accountColor)
        )

        Spacer(Modifier.width(14.dp))

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(accountColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            ItemIconSDefaultIcon(
                iconName = account.icon?.id,
                defaultIcon = R.drawable.ic_custom_account_s,
                tint = accountColor
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 12.dp),
        ) {
            Text(
                text = account.name.value,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = AccountsTheme.typo.b2.copy(
                    color = AccountsTheme.colors.pureInverse,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Start
                )
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.padding(end = 16.dp, top = 12.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                text = if (shouldShortAmount(accountData.balance)) {
                    shortenAmount(accountData.balance)
                } else {
                    accountData.balance.format(currency)
                },
                maxLines = 1,
                style = AccountsTheme.typo.b1.copy(
                    color = AccountsTheme.colors.pureInverse,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.End,
                    fontSize = 18.sp,
                ),
            )

            if (currency != baseCurrency && accountData.balanceBaseCurrency != null) {
                Spacer(Modifier.height(2.dp))

                BalanceRow(
                    modifier = Modifier.testTag("baseCurrencyEquivalent"),
                    currency = baseCurrency,
                    balance = accountData.balanceBaseCurrency!!,
                    textColor = secondaryColor,
                    balanceFontSize = 13.sp,
                    currencyFontSize = 13.sp,
                    currencyUpfront = false,
                    shortenBigNumbers = true,
                )
            }
        }
    }
}

@Composable
private fun DashedAddAccountButton(onClick: () -> Unit) {
    val borderColor = AccountsTheme.colors.medium
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(AccountsTheme.shapes.r4)
            .clickable(onClick = onClick)
            .drawBehind {
                drawRoundRect(
                    color = borderColor,
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f)),
                    ),
                    cornerRadius = CornerRadius(24.dp.toPx()),
                )
            }
            .padding(vertical = 18.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ResourceIcon(
            icon = R.drawable.ic_plus,
            tint = AccountsTheme.colors.pureInverse,
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = stringResource(R.string.add_account),
            style = AccountsTheme.typo.b2.copy(
                color = AccountsTheme.colors.pureInverse,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start
            )
        )
    }
}
