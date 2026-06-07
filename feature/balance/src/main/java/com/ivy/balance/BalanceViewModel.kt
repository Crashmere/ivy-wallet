package com.ivy.balance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.ivy.base.time.TimeConverter
import com.ivy.base.time.TimeProvider
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.legacy.ui.state.PeriodState
import com.ivy.ui.ComposeViewModel
import com.ivy.legacy.ui.model.period.TimePeriod
import com.ivy.domain.usecase.planned.CalculatePlannedPaymentsAmountForRangeUseCase
import com.ivy.domain.usecase.wallet.CalculateWalletBalanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.ZoneOffset
import javax.inject.Inject

@Stable
@HiltViewModel
class BalanceViewModel @Inject constructor(
    private val calculatePlannedPaymentsAmountForRangeUseCase: CalculatePlannedPaymentsAmountForRangeUseCase,
    private val periodState: PeriodState,
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val calculateWalletBalanceUseCase: CalculateWalletBalanceUseCase,
    private val timeProvider: TimeProvider,
    private val timeConverter: TimeConverter,
) : ComposeViewModel<BalanceState, BalanceEvent>() {

    private var period by mutableStateOf(periodState.selectedPeriod)
    private var baseCurrencyCode by mutableStateOf("")
    private var currentBalance by mutableDoubleStateOf(0.0)
    private var plannedPaymentsAmount by mutableDoubleStateOf(0.0)
    private var balanceAfterPlannedPayments by mutableDoubleStateOf(0.0)
    private var numberOfMonthsAhead by mutableIntStateOf(1)

    @Composable
    override fun uiState(): BalanceState {
        LaunchedEffect(Unit) {
            start()
        }

        return BalanceState(
            period = period,
            balanceAfterPlannedPayments = balanceAfterPlannedPayments,
            currentBalance = currentBalance,
            baseCurrencyCode = baseCurrencyCode,
            plannedPaymentsAmount = plannedPaymentsAmount
        )
    }

    override fun onEvent(event: BalanceEvent) {
        when (event) {
            is BalanceEvent.OnNextMonth -> nextMonth()
            is BalanceEvent.OnSetPeriod -> setTimePeriod(event.timePeriod)
            is BalanceEvent.OnPreviousMonth -> previousMonth()
        }
    }

    private fun start(
        timePeriod: TimePeriod = periodState.selectedPeriod
    ) {
        viewModelScope.launch {
            baseCurrencyCode = getBaseCurrencyCode()
            period = timePeriod

            currentBalance = calculateWalletBalanceUseCase(
                baseCurrency = baseCurrencyCode
            ).toDouble()

            plannedPaymentsAmount = calculatePlannedPaymentsAmountForRangeUseCase(
                timePeriod.toRange(periodState.startDayOfMonth, timeConverter, timeProvider)
            ) * if (numberOfMonthsAhead >= 0) {
                numberOfMonthsAhead.toDouble()
            } else {
                1.0
            }
            balanceAfterPlannedPayments =
                currentBalance + plannedPaymentsAmount
        }
    }

    private fun setTimePeriod(timePeriod: TimePeriod) {
        start(timePeriod = timePeriod)
    }

    private fun nextMonth() {
        val month = period.month
        val year = period.year ?: currentUtcYear()
        numberOfMonthsAhead += 1
        if (month != null) {
            val nextPeriod = month.incrementMonthPeriod(
                increment = 1L,
                year = year,
                referenceDate = timeProvider.localDateNow(),
            )
            periodState.select(nextPeriod)
            start(
                timePeriod = nextPeriod
            )
        }
    }

    private fun previousMonth() {
        val month = period.month
        val year = period.year ?: currentUtcYear()
        numberOfMonthsAhead -= 1
        if (month != null) {
            val previousPeriod = month.incrementMonthPeriod(
                increment = -1L,
                year = year,
                referenceDate = timeProvider.localDateNow(),
            )
            periodState.select(previousPeriod)
            start(
                timePeriod = previousPeriod
            )
        }
    }

    private fun currentUtcYear(): Int =
        timeProvider.utcNow().atZone(ZoneOffset.UTC).year
}
