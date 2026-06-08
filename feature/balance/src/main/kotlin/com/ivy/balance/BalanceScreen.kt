package com.ivy.balance

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ivy.ui.navigation.screenScopedViewModel
import com.ivy.ui.platform.LocalDatePicker
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.legacy.ui.theme.style
import com.ivy.data.model.currency.format
import com.ivy.ui.period.LocalPeriodState
import com.ivy.ui.navigation.navigation
import com.ivy.ui.R
import com.ivy.legacy.ui.theme.Gradient
import com.ivy.legacy.ui.theme.Gray
import com.ivy.legacy.ui.theme.Orange
import com.ivy.legacy.ui.theme.White
import com.ivy.legacy.ui.component.BalanceRow
import com.ivy.legacy.ui.component.IvyCircleButton
import com.ivy.legacy.ui.component.IvyDividerLine
import com.ivy.legacy.ui.modal.ChoosePeriodModal
import com.ivy.legacy.ui.modal.ChoosePeriodModalData
import com.ivy.legacy.ui.component.PeriodSelector

private val FabButtonSize = 56.dp

@Composable
fun BoxWithConstraintsScope.BalanceScreen() {
    val viewModel: BalanceViewModel = screenScopedViewModel()
    val uiState = viewModel.uiState()

    UI(
        state = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun BoxWithConstraintsScope.UI(
    state: BalanceState,
    onEvent: (BalanceEvent) -> Unit = {}
) {
    var choosePeriodModal: ChoosePeriodModalData? by remember { mutableStateOf(null) }
    val periodState = LocalPeriodState.current
    val datePicker = LocalDatePicker.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Spacer(Modifier.height(20.dp))

        PeriodSelector(
            period = state.period,
            startDateOfMonth = periodState.startDayOfMonth,
            onPreviousMonth = { onEvent(BalanceEvent.OnPreviousMonth) },
            onNextMonth = { onEvent(BalanceEvent.OnNextMonth) },
            onShowChoosePeriodModal = {
                choosePeriodModal = ChoosePeriodModalData(
                    period = state.period
                )
            }
        )

        Spacer(Modifier.height(32.dp))

        CurrentBalance(
            currency = state.baseCurrencyCode,
            currentBalance = state.currentBalance
        )

        Spacer(Modifier.height(32.dp))

        IvyDividerLine(
            modifier = Modifier
                .padding(horizontal = 24.dp)
        )

        Spacer(Modifier.height(40.dp))

        BalanceAfterPlannedPayments(
            currency = state.baseCurrencyCode,
            currentBalance = state.currentBalance,
            plannedPaymentsAmount = state.plannedPaymentsAmount,
            balanceAfterPlannedPayments = state.balanceAfterPlannedPayments
        )

        Spacer(Modifier.weight(1f))

        CloseButton()

        Spacer(Modifier.height(48.dp))
    }

    ChoosePeriodModal(
        modal = choosePeriodModal,
        dismiss = {
            choosePeriodModal = null
        },
        saveSelectedPeriod = periodState::select,
        pickDate = { minDate, maxDate, initialDate, onDatePicked ->
            datePicker.pickDate(
                minDate = minDate,
                maxDate = maxDate,
                initialDate = initialDate,
                onDatePicked = onDatePicked
            )
        },
    ) {
        onEvent(BalanceEvent.OnSetPeriod(it))
    }
}

@Composable
private fun ColumnScope.CurrentBalance(
    currency: String,
    currentBalance: Double
) {
    Text(
        modifier = Modifier.align(Alignment.CenterHorizontally),
        text = stringResource(R.string.current_balance),
        style = LegacyTheme.typo.b2.style(
            color = Gray,
            fontWeight = FontWeight.ExtraBold
        )
    )

    Spacer(Modifier.height(4.dp))

    BalanceRow(
        modifier = Modifier.align(Alignment.CenterHorizontally),
        currency = currency,
        balance = currentBalance
    )
}

@Composable
private fun ColumnScope.BalanceAfterPlannedPayments(
    currency: String,
    currentBalance: Double,
    plannedPaymentsAmount: Double,
    balanceAfterPlannedPayments: Double
) {
    Text(
        modifier = Modifier
            .padding(horizontal = 32.dp),
        text = stringResource(R.string.balance_after_payments),
        style = LegacyTheme.typo.b2.style(
            color = Orange,
            fontWeight = FontWeight.ExtraBold
        )
    )

    Spacer(Modifier.height(8.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(32.dp))

        BalanceRow(
            currency = currency,
            balance = balanceAfterPlannedPayments,

            balanceFontSize = 30.sp,
            currencyFontSize = 18.sp,

            currencyUpfront = false
        )

        Spacer(Modifier.weight(1f))

        Column(
            horizontalAlignment = Alignment.End,
        ) {
            Spacer(Modifier.height(4.dp))

            Text(
                text = "${currentBalance.format(2)} $currency",
                style = LegacyTheme.typo.nC.style(
                    color = LegacyTheme.colors.pureInverse,
                    fontWeight = FontWeight.Normal
                )
            )

            Spacer(Modifier.height(2.dp))

            val plusSign = if (plannedPaymentsAmount >= 0) "+" else ""
            Text(
                text = "${plusSign}${plannedPaymentsAmount.format(2)} $currency",
                style = LegacyTheme.typo.nC.style(
                    color = LegacyTheme.colors.pureInverse,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }

        Spacer(Modifier.width(32.dp))
    }
}

@Composable
private fun ColumnScope.CloseButton() {
    val nav = navigation()
    IvyCircleButton(
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .size(FabButtonSize)
            .rotate(45f)
            .zIndex(200f),
        backgroundPadding = 8.dp,
        icon = R.drawable.ic_add,
        backgroundGradient = Gradient.solid(Gray),
        hasShadow = false,
        tint = White
    ) {
        nav.back()
    }
}
