package com.ivy.loans.loan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.ivy.data.model.processByType
import com.ivy.data.model.LoanType
import com.ivy.domain.usecase.account.CreateAccountWithBalanceUseCase
import com.ivy.domain.usecase.account.GetLastSelectedAccountIdUseCase
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.domain.usecase.loan.CreateLoanUseCase
import com.ivy.domain.usecase.loan.GetLoanRecordsUseCase
import com.ivy.domain.usecase.loan.GetLoansUseCase
import com.ivy.domain.usecase.loan.LoanTransactionSyncUseCase
import com.ivy.domain.usecase.loan.ReorderLoansUseCase
import com.ivy.data.model.legacy.Account
import com.ivy.data.model.legacy.Loan
import com.ivy.data.model.currency.format
import com.ivy.data.model.currency.getDefaultFIATCurrency
import com.ivy.loans.loan.data.DisplayLoan
import com.ivy.ui.ComposeViewModel
import com.ivy.ui.time.DateTimePicker
import com.ivy.domain.usecase.account.GetLegacyAccountsUseCase
import com.ivy.data.model.CreateAccountData
import com.ivy.data.model.legacy.CreateLoanData
import com.ivy.legacy.ui.modal.LoanModalData
import com.ivy.loans.nowLocalDate
import com.ivy.loans.nowLocalTime
import com.ivy.loans.nowUtc
import com.ivy.loans.toUtcInstant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

@Stable
@HiltViewModel
class LoanViewModel @Inject constructor(
    private val getBaseCurrencyCodeUseCase: GetBaseCurrencyCodeUseCase,
    private val getLoanRecordsUseCase: GetLoanRecordsUseCase,
    private val reorderLoansUseCase: ReorderLoansUseCase,
    private val createLoanUseCase: CreateLoanUseCase,
    private val getLastSelectedAccountId: GetLastSelectedAccountIdUseCase,
    private val createAccountWithBalanceUseCase: CreateAccountWithBalanceUseCase,
    private val loanTransactionSyncUseCase: LoanTransactionSyncUseCase,
    private val getLoansUseCase: GetLoansUseCase,
    private val getLegacyAccountsUseCase: GetLegacyAccountsUseCase,
    private val dateTimePicker: DateTimePicker
) : ComposeViewModel<LoanScreenState, LoanScreenEvent>() {

    private var baseCurrencyCode by mutableStateOf(getDefaultFIATCurrency().currencyCode)
    private var completedLoans by mutableStateOf<ImmutableList<DisplayLoan>>(persistentListOf())
    private var pendingLoans by mutableStateOf<ImmutableList<DisplayLoan>>(persistentListOf())
    private var accounts by mutableStateOf<ImmutableList<Account>>(persistentListOf())
    private var selectedAccount by mutableStateOf<Account?>(null)
    private var loanModalData by mutableStateOf<LoanModalData?>(null)
    private var reorderModalVisible by mutableStateOf(false)
    private var dateTime by mutableStateOf<Instant>(nowUtc())
    private var selectedTab by mutableStateOf(LoanTab.PENDING)

    /** If true paid off loans will be visible */
    private var paidOffLoanVisibility by mutableStateOf(true)

    /** Contains all loans including both paidOff and pending*/
    private var allLoans: ImmutableList<DisplayLoan> = persistentListOf()
    private var defaultCurrencyCode = ""
    private var totalOweAmount = 0.0
    private var totalOwedAmount = 0.0

    @Composable
    override fun uiState(): LoanScreenState {
        LaunchedEffect(Unit) {
            start()
        }

        return LoanScreenState(
            baseCurrency = getBaseCurrencyCode(),
            accounts = getAccounts(),
            selectedAccount = getSelectedAccount(),
            loanModalData = getLoanModalData(),
            reorderModalVisible = getReorderModalVisible(),
            totalOweAmount = getTotalOweAmount(totalOweAmount, defaultCurrencyCode),
            totalOwedAmount = getTotalOwedAmount(totalOwedAmount, defaultCurrencyCode),
            paidOffLoanVisibility = getPaidOffLoanVisibility(),
            dateTime = dateTime,
            selectedTab = getSelectedTab(),
            completedLoans = getCompletedLoans(),
            pendingLoans = getPendingLoans()
        )
    }

    fun setTab(tab: LoanTab) {
        selectedTab = tab
    }

    @Composable
    private fun getSelectedTab(): LoanTab {
        return selectedTab
    }

    @Composable
    private fun getCompletedLoans(): ImmutableList<DisplayLoan> {
        return completedLoans
    }

    @Composable
    private fun getPendingLoans(): ImmutableList<DisplayLoan> {
        return pendingLoans
    }

    @Composable
    private fun getReorderModalVisible() = reorderModalVisible

    @Composable
    private fun getLoanModalData() = loanModalData

    @Composable
    private fun getBaseCurrencyCode(): String {
        return baseCurrencyCode
    }

    @Composable
    private fun getSelectedAccount() = selectedAccount

    @Composable
    private fun getAccounts() = accounts

    @Composable
    private fun getPaidOffLoanVisibility(): Boolean = paidOffLoanVisibility

    override fun onEvent(event: LoanScreenEvent) {
        when (event) {
            is LoanScreenEvent.OnLoanCreate -> {
                createLoan(event.createLoanData)
            }

            is LoanScreenEvent.OnAddLoan -> {
                loanModalData = LoanModalData(
                    loan = null,
                    baseCurrency = baseCurrencyCode,
                    selectedAccount = selectedAccount
                )
            }

            is LoanScreenEvent.OnLoanModalDismiss -> {
                loanModalData = null
                dateTime = nowUtc()
            }

            is LoanScreenEvent.OnReOrderModalShow -> {
                reorderModalVisible = event.show
            }

            is LoanScreenEvent.OnReordered -> {
                reorder(event.reorderedList)
            }

            is LoanScreenEvent.OnCreateAccount -> {
                createAccount(event.accountData)
            }

            LoanScreenEvent.OnTogglePaidOffLoanVisibility -> {
                updatePaidOffLoanVisibility()
            }

            is LoanScreenEvent.OnChangeDate -> {
                handleChangeDate()
            }

            is LoanScreenEvent.OnChangeTime -> {
                handleChangeTime()
            }

            is LoanScreenEvent.OnTabChanged -> {
                setTab(event.tab)
            }
        }
    }

    private fun start() {
        viewModelScope.launch(Dispatchers.Default) {

            dateTime = nowUtc()

            defaultCurrencyCode = getBaseCurrencyCodeUseCase().also {
                baseCurrencyCode = it
            }

            initialiseAccounts()

            totalOweAmount = 0.0
            totalOwedAmount = 0.0

            allLoans = withContext(Dispatchers.IO) {
                getLoansUseCase()
                    .map { loan ->
                        val (amountPaid, loanTotalAmount) = calculateAmountPaidAndTotalAmount(loan)
                        val percentPaid = if (loanTotalAmount != 0.0) {
                            amountPaid / loanTotalAmount
                        } else {
                            0.0
                        }
                        var currCode = findCurrencyCode(accounts, loan.accountId)

                        when (loan.type) {
                            LoanType.BORROW -> totalOweAmount += (loanTotalAmount - amountPaid)
                            LoanType.LEND -> totalOwedAmount += (loanTotalAmount - amountPaid)
                        }

                        DisplayLoan(
                            loan = loan,
                            loanTotalAmount = loanTotalAmount,
                            amountPaid = amountPaid,
                            currencyCode = currCode,
                            formattedDisplayText = "${amountPaid.format(currCode)} $currCode / ${
                                loanTotalAmount.format(
                                    currCode
                                )
                            } $currCode (${
                                percentPaid.times(
                                    100
                                ).format(2)
                            }%)",
                            percentPaid = percentPaid
                        )
                    }.toImmutableList()
            }
            loadPendingLoans()
            loadCompletedLoans()

        }
    }

    private fun getTotalOwedAmount(totalOwedAmount: Double, currCode: String): String {
        return if (totalOwedAmount != 0.0) {
            "${totalOwedAmount.format(currCode)} $currCode"
        } else {
            ""
        }
    }

    private fun getTotalOweAmount(totalOweAmount: Double, currCode: String): String {
        return if (totalOweAmount != 0.0) {
            "${totalOweAmount.format(currCode)} $currCode"
        } else {
            ""
        }
    }

    private suspend fun initialiseAccounts() {
        val accountsList = getLegacyAccountsUseCase()
        accounts = accountsList
        selectedAccount = defaultAccountId(accountsList)
        selectedAccount?.let {
            baseCurrencyCode = it.currency ?: defaultCurrencyCode
        }
    }

    private fun handleChangeDate() {
        dateTimePicker.pickDate(
            initialDate = loanModalData?.loan?.dateTime?.let {
                it.toUtcInstant()
            } ?: nowUtc()
        ) { localDate ->
            val localTime = loanModalData?.loan?.dateTime?.let {
                it.toLocalTime()
            } ?: nowLocalTime()

            updateDateTime(localDate.atTime(localTime))
        }
    }

    private fun handleChangeTime() {
        dateTimePicker.pickTime(
            initialTime = loanModalData?.loan?.dateTime?.let {
                it.toLocalTime()
            } ?: nowLocalTime()
        ) { localTime ->
            val localDate = loanModalData?.loan?.dateTime?.let {
                it.toLocalDate()
            } ?: nowLocalDate()

            updateDateTime(localDate.atTime(localTime))
        }
    }

    private fun updateDateTime(newDateTime: LocalDateTime) {
        val newDateTimeUtc = newDateTime.toUtcInstant()
        loanModalData?.let { currentData ->
            loanModalData = currentData.copy(
                loan = currentData.loan?.copy(
                    dateTime = newDateTime
                )
            )
            dateTime = newDateTimeUtc
        }
    }

    private fun createLoan(data: CreateLoanData) {
        viewModelScope.launch {

            val loan = createLoanUseCase(data)
            if (loan != null) {
                start()

                loanTransactionSyncUseCase.createAssociatedLoanTransaction(
                    data = data,
                    loanId = loan.id
                )
            }

        }
    }

    private fun reorder(newOrder: List<DisplayLoan>) {
        viewModelScope.launch {
            reorderLoansUseCase(newOrder.map(DisplayLoan::loan))
            start()
        }
    }

    private fun loadCompletedLoans() {
        completedLoans = allLoans.filter { loan -> loan.percentPaid >= 1.0 }.toImmutableList()
    }

    private fun loadPendingLoans() {
        pendingLoans = allLoans.filter { loan -> loan.percentPaid < 1.0 }.toImmutableList()
    }

    private fun createAccount(data: CreateAccountData) {
        viewModelScope.launch {
            createAccountWithBalanceUseCase(data)
            accounts = getLegacyAccountsUseCase()
        }
    }

    private fun defaultAccountId(
        accounts: List<Account>,
    ): Account? {
        val lastSelectedId = getLastSelectedAccountId()

        lastSelectedId?.let { uuid ->
            return accounts.find { it.id == uuid }
        } ?: run {
            return if (accounts.isNotEmpty()) accounts[0] else null
        }
    }

    private fun findCurrencyCode(accounts: List<Account>, accountId: UUID?): String {
        return accountId?.let {
            accounts.find { account -> account.id == it }?.currency
        } ?: defaultCurrencyCode
    }

    /**
     *  Calculates the total amount paid and the total loan amount including any changes made to the loan.
     *  @return A Pair containing the total amount paid and the total loan amount.
     */
    private suspend fun calculateAmountPaidAndTotalAmount(loan: Loan): Pair<Double, Double> {
        val loanRecords = getLoanRecordsUseCase(loan.id)
        val (amountPaid, loanTotalAmount) = loanRecords.fold(0.0 to loan.amount) { value, loanRecord ->
            val (currentAmountPaid, currentLoanTotalAmount) = value
            if (loanRecord.interest) return@fold value
            val convertedAmount = loanRecord.convertedAmount ?: loanRecord.amount

            loanRecord.loanRecordType.processByType(
                decreaseAction = { currentAmountPaid + convertedAmount to currentLoanTotalAmount },
                increaseAction = { currentAmountPaid to currentLoanTotalAmount + convertedAmount }
            )
        }
        return amountPaid to loanTotalAmount
    }

    private fun updatePaidOffLoanVisibility() {
        paidOffLoanVisibility = paidOffLoanVisibility.not()
    }
}
