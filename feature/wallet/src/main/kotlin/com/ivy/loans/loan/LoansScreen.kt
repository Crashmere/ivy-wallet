package com.ivy.loans.loan

import com.ivy.loans.LoansTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ivy.ui.navigation.screenScopedViewModel
import com.ivy.loans.humanReadableType
import com.ivy.data.model.currency.getDefaultFIATCurrency
import com.ivy.ui.compose.horizontalSwipeListener
import com.ivy.ui.compose.rememberSwipeListenerState
import com.ivy.loans.loan.Constants.SWIPE_HORIZONTAL_THRESHOLD
import com.ivy.loans.model.DisplayLoan
import com.ivy.ui.navigation.LoanDetailsScreen
import com.ivy.ui.navigation.navigation
import com.ivy.ui.R
import com.ivy.ui.rememberScrollPositionListState
import com.ivy.ui.money.formatAmount
import com.ivy.ui.icon.ItemIconSDefaultIcon
import com.ivy.ui.modal.ReorderModalSingleType
import com.ivy.ui.compose.FilledIconButton
import com.ivy.ui.compose.ResourceIcon
import com.ivy.ui.theme.colors.findContrastTextColor
import com.ivy.loans.modal.LoanModal
import com.ivy.ui.theme.colors.toComposeColor
import com.ivy.loans.LoanProgressBar
import kotlin.math.roundToInt

@Composable
fun BoxWithConstraintsScope.LoansScreen() {
    val viewModel: LoanViewModel = screenScopedViewModel()
    val state = viewModel.uiState()
    UI(
        state = state,
        onEventHandler = viewModel::onEvent
    )
}

@Composable
private fun BoxWithConstraintsScope.UI(
    state: LoanScreenState,
    onEventHandler: (LoanScreenEvent) -> Unit = {}
) {
    val nav = navigation()
    var loanModalVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .horizontalSwipeListener(
                sensitivity = SWIPE_HORIZONTAL_THRESHOLD,
                state = rememberSwipeListenerState(),
                onSwipeLeft = {
                    onEventHandler.invoke(LoanScreenEvent.OnTabChanged(LoanTab.COMPLETED))
                },
                onSwipeRight = {
                    onEventHandler.invoke(LoanScreenEvent.OnTabChanged(LoanTab.PENDING))
                }
            )
    ) {
        Spacer(Modifier.height(20.dp))

        Toolbar(
            onDismiss = { nav.back() },
            setReorderModalVisible = { onEventHandler.invoke(LoanScreenEvent.OnReOrderModalShow(show = it)) },
            state.totalOweAmount,
            state.totalOwedAmount
        )

        Spacer(Modifier.height(8.dp))

        val scrollState = rememberScrollPositionListState(
            key = "loans_lazy_column"
        )

        val loans = if (state.selectedTab == LoanTab.PENDING) {
            state.pendingLoans
        } else {
            state.completedLoans
        }

        LazyColumn(state = scrollState) {
            items(loans) { item ->
                Spacer(Modifier.height(12.dp))

                LoanItem(
                    displayLoan = item
                ) {
                    nav.navigateTo(
                        screen = LoanDetailsScreen(
                            loanId = item.loan.id
                        )
                    )
                }
            }

            item {
                Spacer(Modifier.height(150.dp)) // scroll hack
            }
        }

        if (loans.isEmpty()) {
            Spacer(Modifier.weight(1f))

            NoLoansEmptyState(
                emptyStateTitle = stringResource(R.string.no_loans),
                emptyStateText = stringResource(R.string.no_loans_description)
            )

            Spacer(Modifier.weight(1f))
        }

        Spacer(Modifier.height(150.dp)) // scroll hack
    }

    LoanBottomBar(
        tab = state.selectedTab,
        selectTab = { onEventHandler.invoke(LoanScreenEvent.OnTabChanged(it)) },
        onAdd = {
            loanModalVisible = true
        }
    )

    ReorderModalSingleType(
        visible = state.reorderModalVisible,
        initialItems = if (state.selectedTab == LoanTab.PENDING) state.pendingLoans else state.completedLoans,
        itemOrderNum = { it.loan.orderNum },
        withNewOrderNum = { item, newOrderNum ->
            item.copy(
                loan = item.loan.copy(
                    orderNum = newOrderNum
                )
            )
        },
        dismiss = {
            onEventHandler.invoke(LoanScreenEvent.OnReOrderModalShow(show = false))
        },
        onReordered = {
            onEventHandler.invoke(LoanScreenEvent.OnReordered(loanIds = it.map { item -> item.loan.id }))
        }
    ) { _, item ->
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 24.dp)
                .padding(vertical = 8.dp),
            text = item.loan.name,
            style = LoansTheme.typo.b1.copy(
                color = LoansTheme.colors.pureInverse,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start
            )
        )
    }

    LoanModal(
        visible = loanModalVisible,
        loan = null,
        baseCurrency = state.baseCurrency,
        selectedAccountId = state.selectedAccountId,
        accounts = state.accounts,
        onCreateAccount = {
            onEventHandler.invoke(LoanScreenEvent.OnCreateAccount(accountData = it))
        },
        onCreateLoan = {
            onEventHandler.invoke(LoanScreenEvent.OnLoanCreate(createLoanData = it))
        },
        onEditLoan = { _, _ -> },
        dismiss = {
            loanModalVisible = false
            onEventHandler.invoke(LoanScreenEvent.OnLoanModalDismiss)
        },
        dateTime = state.dateTime,
        onSetDate = {
            onEventHandler.invoke(LoanScreenEvent.OnChangeDate)
        },
        onSetTime = {
            onEventHandler.invoke(LoanScreenEvent.OnChangeTime)
        }
    )
}

@Composable
private fun Toolbar(
    onDismiss: () -> Unit,
    setReorderModalVisible: (Boolean) -> Unit,
    totalOweAmount: String,
    totalOwedAmount: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 24.dp, end = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.loans),
                style = LoansTheme.typo.h2.copy(
                    color = LoansTheme.colors.pureInverse,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Start
                )
            )
            if (totalOweAmount.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.you_owe, totalOweAmount),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
            if (totalOwedAmount.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.you_are_owed, totalOwedAmount),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        FilledIconButton(
            modifier = Modifier,
            icon = R.drawable.ic_dismiss,
            backgroundColor = LoansTheme.colors.medium,
            tint = LoansTheme.colors.pureInverse,
            onClick = onDismiss
        )

        Spacer(Modifier.width(8.dp))

        FilledIconButton(
            icon = R.drawable.ic_drag_handle,
            contentDescription = "reorder",
            backgroundColor = LoansTheme.colors.medium,
            tint = LoansTheme.colors.pureInverse,
        ) {
            setReorderModalVisible(true)
        }

        Spacer(Modifier.width(24.dp))
    }
}

@Composable
private fun LoanItem(
    displayLoan: DisplayLoan,
    onClick: () -> Unit
) {
    val loan = displayLoan.loan
    val loanColor = loan.color.toComposeColor()
    val currency = displayLoan.currencyCode ?: getDefaultFIATCurrency().currencyCode
    val leftToPay = displayLoan.loanTotalAmount - displayLoan.amountPaid
    val percent = displayLoan.percentPaid

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(LoansTheme.shapes.r4)
            .border(2.dp, LoansTheme.colors.medium, LoansTheme.shapes.r4)
            .testTag("loan_item")
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(loanColor),
                contentAlignment = Alignment.Center
            ) {
                ItemIconSDefaultIcon(
                    iconName = loan.icon,
                    defaultIcon = R.drawable.ic_custom_loan_s,
                    tint = findContrastTextColor(loanColor)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = loan.name,
                    style = LoansTheme.typo.b2.copy(
                        color = LoansTheme.colors.pureInverse,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Start
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = loan.humanReadableType(),
                    style = LoansTheme.typo.nC.copy(
                        color = LoansTheme.colors.gray,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Start
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(12.dp))

            Text(
                text = "${formatAmount(leftToPay, currency)} $currency",
                style = LoansTheme.typo.nB2.copy(
                    color = LoansTheme.colors.pureInverse,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.End
                ),
                maxLines = 1
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LoanProgressBar(
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp),
                notFilledColor = LoansTheme.colors.medium,
                percent = percent
            )

            Spacer(Modifier.width(10.dp))

            Text(
                text = "${(percent * 100).roundToInt()}%",
                style = LoansTheme.typo.nC.copy(
                    color = LoansTheme.colors.gray,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End
                )
            )
        }
    }
}

@Composable
private fun NoLoansEmptyState(
    emptyStateTitle: String,
    emptyStateText: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))

        ResourceIcon(
            icon = R.drawable.ic_custom_loan_l,
            tint = LoansTheme.colors.gray
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = emptyStateTitle,
            style = LoansTheme.typo.b1.copy(
                color = LoansTheme.colors.gray,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Start
            )
        )

        Spacer(Modifier.height(8.dp))

        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = emptyStateText,
            style = LoansTheme.typo.b2.copy(
                color = LoansTheme.colors.gray,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        )

        Spacer(Modifier.height(96.dp))
    }
}
