package com.ivy.loans.loandetails

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.ivy.data.model.LoanRecordType
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.domain.usecase.loan.CreateLoanRecordUseCase
import com.ivy.domain.usecase.loan.DeleteLoanRecordUseCase
import com.ivy.domain.usecase.loan.DeleteLoanUseCase
import com.ivy.domain.usecase.loan.GetLoanRecordsUseCase
import com.ivy.domain.usecase.loan.GetLoanTransactionUseCase
import com.ivy.domain.usecase.loan.GetLoanUseCase
import com.ivy.domain.usecase.loan.HasLoanRecordTransactionUseCase
import com.ivy.domain.usecase.loan.LoanRecordTransactionSyncUseCase
import com.ivy.domain.usecase.loan.LoanTransactionSyncUseCase
import com.ivy.domain.usecase.loan.UpdateLoanRecordUseCase
import com.ivy.domain.usecase.loan.UpdateLoanUseCase
import com.ivy.domain.usecase.account.CreateAccountWithBalanceUseCase
import com.ivy.domain.usecase.account.GetAccountsUseCase
import com.ivy.data.model.Loan
import com.ivy.data.model.LoanRecord
import com.ivy.loans.model.DisplayLoanAccount
import com.ivy.loans.model.DisplayLoanRecord
import com.ivy.loans.model.LoanAccount
import com.ivy.loans.model.toLoanAccount
import com.ivy.loans.loandetails.events.DeleteLoanModalEvent
import com.ivy.loans.loandetails.events.LoanDetailsScreenEvent
import com.ivy.loans.loandetails.events.LoanModalEvent
import com.ivy.loans.loandetails.events.LoanRecordModalEvent
import com.ivy.ui.ComposeViewModel
import com.ivy.ui.time.DateTimePicker
import com.ivy.data.model.CreateAccountData
import com.ivy.data.model.CreateLoanRecordData
import com.ivy.data.model.EditLoanRecordData
import com.ivy.loans.nowLocalDate
import com.ivy.loans.nowLocalTime
import com.ivy.loans.nowUtc
import com.ivy.loans.toLocalDateInSystemZone
import com.ivy.loans.toLocalTimeInSystemZone
import com.ivy.loans.toUtcInstant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

@Stable
@HiltViewModel
internal class LoanDetailsViewModel @Inject internal constructor(
    private val updateLoanUseCase: UpdateLoanUseCase,
    private val deleteLoanUseCase: DeleteLoanUseCase,
    private val createLoanRecordUseCase: CreateLoanRecordUseCase,
    private val updateLoanRecordUseCase: UpdateLoanRecordUseCase,
    private val deleteLoanRecordUseCase: DeleteLoanRecordUseCase,
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val getLoanRecordsUseCase: GetLoanRecordsUseCase,
    private val getLoanTransactionUseCase: GetLoanTransactionUseCase,
    private val hasLoanRecordTransactionUseCase: HasLoanRecordTransactionUseCase,
    private val createAccountWithBalanceUseCase: CreateAccountWithBalanceUseCase,
    private val loanTransactionSyncUseCase: LoanTransactionSyncUseCase,
    private val loanRecordTransactionSyncUseCase: LoanRecordTransactionSyncUseCase,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getLoanUseCase: GetLoanUseCase,
    private val dateTimePicker: DateTimePicker,
) : ComposeViewModel<LoanDetailsScreenState, LoanDetailsScreenEvent>() {
    private data class LoanRecordTotals(
        val amountPaid: Double,
        val interestAmountPaid: Double,
        val totalAmount: Double,
    )

    private val baseCurrency = mutableStateOf("")
    private val loan = mutableStateOf<Loan?>(null)
    private val displayLoanRecords =
        mutableStateOf<ImmutableList<DisplayLoanRecord>>(persistentListOf())
    private val loanTotalAmount = mutableDoubleStateOf(0.0)
    private val amountPaid = mutableDoubleStateOf(0.0)
    private val accounts = mutableStateOf<ImmutableList<LoanAccount>>(persistentListOf())
    private val loanInterestAmountPaid = mutableDoubleStateOf(0.0)
    private val selectedLoanAccountId = mutableStateOf<UUID?>(null)
    private val createLoanTransaction = mutableStateOf(false)
    private var defaultCurrencyCode = ""
    private val loanModalVisible = mutableStateOf(false)
    private val loanModalLoan = mutableStateOf<Loan?>(null)
    private val loanModalAutoFocusKeyboard = mutableStateOf(true)
    private val loanModalAutoOpenAmountModal = mutableStateOf(false)
    private val loanRecordModalVisible = mutableStateOf(false)
    private val loanRecordModalLoanRecord = mutableStateOf<LoanRecord?>(null)
    private val loanRecordModalBaseCurrency = mutableStateOf("")
    private val loanRecordModalLoanAccountCurrencyCode = mutableStateOf<String?>(null)
    private val loanRecordModalSelectedAccountId = mutableStateOf<UUID?>(null)
    private val loanRecordModalCreateTransaction = mutableStateOf(false)
    private val loanRecordModalIsLoanInterest = mutableStateOf(false)
    private val waitModalVisible = mutableStateOf(false)
    private val isDeleteModalVisible = mutableStateOf(false)
    private val dateTime = mutableStateOf<Instant>(nowUtc())
    private val _uiEvents = MutableSharedFlow<LoanDetailsUiEvent>()
    val uiEvents: SharedFlow<LoanDetailsUiEvent> = _uiEvents.asSharedFlow()

    @Composable
    override fun uiState(): LoanDetailsScreenState {
        return LoanDetailsScreenState(
            baseCurrency = baseCurrency.value,
            loan = loan.value,
            displayLoanRecords = displayLoanRecords.value,
            loanTotalAmount = loanTotalAmount.doubleValue,
            amountPaid = amountPaid.doubleValue,
            loanAmountPaid = loanInterestAmountPaid.doubleValue,
            accounts = accounts.value,
            selectedLoanAccountId = selectedLoanAccountId.value,
            createLoanTransaction = createLoanTransaction.value,
            loanModalVisible = loanModalVisible.value,
            loanModalLoan = loanModalLoan.value,
            loanModalAutoFocusKeyboard = loanModalAutoFocusKeyboard.value,
            loanModalAutoOpenAmountModal = loanModalAutoOpenAmountModal.value,
            loanRecordModalVisible = loanRecordModalVisible.value,
            loanRecordModalLoanRecord = loanRecordModalLoanRecord.value,
            loanRecordModalBaseCurrency = loanRecordModalBaseCurrency.value,
            loanRecordModalLoanAccountCurrencyCode = loanRecordModalLoanAccountCurrencyCode.value,
            loanRecordModalSelectedAccountId = loanRecordModalSelectedAccountId.value,
            loanRecordModalCreateTransaction = loanRecordModalCreateTransaction.value,
            loanRecordModalIsLoanInterest = loanRecordModalIsLoanInterest.value,
            waitModalVisible = waitModalVisible.value,
            isDeleteModalVisible = isDeleteModalVisible.value,
            dateTime = dateTime.value
        )
    }

    override fun onEvent(event: LoanDetailsScreenEvent) {
        when (event) {
            is LoanRecordModalEvent -> handleLoanRecordModalEvents(event)
            is LoanModalEvent -> handleLoanModalEvents(event)
            is DeleteLoanModalEvent -> handleDeleteLoanModalEvents(event)
            else -> handleLoanDetailsScreenEvents(event)
        }
    }

    private fun handleLoanRecordModalEvents(event: LoanDetailsScreenEvent) {
        when (event) {
            is LoanRecordModalEvent.OnClickLoanRecord -> {
                val displayLoanRecord = displayLoanRecordById(event.loanRecordId) ?: return
                showLoanRecordModal(
                    loanRecord = displayLoanRecord.loanRecord,
                    baseCurrency = displayLoanRecord.loanRecordCurrencyCode,
                    selectedAccountId = displayLoanRecord.account?.id,
                    createLoanRecordTransaction = displayLoanRecord.loanRecordTransaction,
                    isLoanInterest = displayLoanRecord.loanRecord.interest,
                    loanAccountCurrencyCode = displayLoanRecord.loanCurrencyCode
                )
            }

            is LoanRecordModalEvent.OnCreateLoanRecord -> {
                createLoanRecord(event.loanRecordData)
            }

            is LoanRecordModalEvent.OnDeleteLoanRecord -> {
                deleteLoanRecord(event.loanRecordId)
            }

            LoanRecordModalEvent.OnDismissLoanRecord -> {
                resetLoanRecordModal()
                dateTime.value = nowUtc()
            }

            is LoanRecordModalEvent.OnEditLoanRecord -> {
                editLoanRecord(event.loanRecordData)
            }

            is LoanRecordModalEvent.OnChangeDate -> {
                handleChangeDate()
            }
            is LoanRecordModalEvent.OnChangeTime -> {
                handleChangeTime()
            }
            else -> {}
        }
    }

    private fun handleLoanModalEvents(event: LoanDetailsScreenEvent) {
        when (event) {
            LoanModalEvent.OnDismissLoanModal -> {
                loanModalVisible.value = false
                loanModalLoan.value = null
                loanModalAutoFocusKeyboard.value = true
                loanModalAutoOpenAmountModal.value = false
                dateTime.value = nowUtc()
            }

            is LoanModalEvent.OnEditLoanModal -> {
                editLoan(event.loan, event.createLoanTransaction)
            }

            LoanModalEvent.PerformCalculation -> {
                waitModalVisible.value = true
            }

            LoanModalEvent.OnChangeDate -> {
                handleLoanChangeDate()
            }

            LoanModalEvent.OnChangeTime -> {
                handleLoanChangeTime()
            }

            else -> {}
        }
    }

    private fun handleDeleteLoanModalEvents(event: LoanDetailsScreenEvent) {
        when (event) {
            DeleteLoanModalEvent.OnDeleteLoan -> {
                deleteLoan()
                isDeleteModalVisible.value = false
            }

            is DeleteLoanModalEvent.OnDismissDeleteLoan -> {
                isDeleteModalVisible.value = event.isDeleteModalVisible
            }

            else -> {}
        }
    }

    private fun handleLoanDetailsScreenEvents(event: LoanDetailsScreenEvent) {
        when (event) {
            LoanDetailsScreenEvent.OnAmountClick -> {
                showLoanModal(
                    autoFocusKeyboard = false,
                    autoOpenAmountModal = true
                )
            }

            LoanDetailsScreenEvent.OnEditLoanClick -> {
                showLoanModal(
                    autoFocusKeyboard = false,
                    autoOpenAmountModal = false
                )
            }

            LoanDetailsScreenEvent.OnAddRecord -> {
                showLoanRecordModal(
                    loanRecord = null,
                    baseCurrency = baseCurrency.value,
                    selectedAccountId = selectedLoanAccountId.value
                )
            }

            is LoanDetailsScreenEvent.OnCreateAccount -> {
                createAccount(event.data)
            }

            else -> {}
        }
    }

    private fun showLoanRecordModal(
        loanRecord: LoanRecord?,
        baseCurrency: String,
        loanAccountCurrencyCode: String? = null,
        selectedAccountId: UUID? = null,
        createLoanRecordTransaction: Boolean = false,
        isLoanInterest: Boolean = false,
    ) {
        loanRecordModalLoanRecord.value = loanRecord
        loanRecordModalBaseCurrency.value = baseCurrency
        loanRecordModalLoanAccountCurrencyCode.value = loanAccountCurrencyCode
        loanRecordModalSelectedAccountId.value = selectedAccountId
        loanRecordModalCreateTransaction.value = createLoanRecordTransaction
        loanRecordModalIsLoanInterest.value = isLoanInterest
        loanRecordModalVisible.value = true
    }

    private fun resetLoanRecordModal() {
        loanRecordModalVisible.value = false
        loanRecordModalLoanRecord.value = null
        loanRecordModalBaseCurrency.value = ""
        loanRecordModalLoanAccountCurrencyCode.value = null
        loanRecordModalSelectedAccountId.value = null
        loanRecordModalCreateTransaction.value = false
        loanRecordModalIsLoanInterest.value = false
    }

    private fun showLoanModal(
        autoFocusKeyboard: Boolean,
        autoOpenAmountModal: Boolean,
    ) {
        loanModalLoan.value = loan.value
        loanModalAutoFocusKeyboard.value = autoFocusKeyboard
        loanModalAutoOpenAmountModal.value = autoOpenAmountModal
        loanModalVisible.value = true
    }

    fun start(loanId: UUID) {
        load(loanId = loanId)
    }

    private fun load(loanId: UUID) {
        viewModelScope.launch {

            dateTime.value = nowUtc()

            defaultCurrencyCode = getBaseCurrencyCode().also {
                baseCurrency.value = it
            }

            accounts.value = loadAccounts()

            loan.value = getLoanUseCase(loanId)
            selectedLoanAccountId.value = loan.value?.accountId

            loan.value?.let { loan ->
                selectedLoanAccountCurrencyCode()?.let { currencyCode ->
                    baseCurrency.value = currencyCode
                }
            }

            withContext(Dispatchers.Default) {
                displayLoanRecords.value =
                    getLoanRecordsUseCase(loanId).map {
                        val hasTransaction = hasLoanRecordTransactionUseCase(it.id)

                        val account = findAccount(
                            accounts = accounts.value,
                            accountId = it.accountId,
                        )

                        DisplayLoanRecord(
                            it,
                            account = account?.let { accountData ->
                                DisplayLoanAccount(
                                    id = accountData.id,
                                    name = accountData.name,
                                    icon = accountData.icon
                                )
                            },
                            loanRecordTransaction = hasTransaction,
                            loanRecordCurrencyCode = account?.currency ?: defaultCurrencyCode,
                            loanCurrencyCode = selectedLoanAccountCurrencyCode() ?: defaultCurrencyCode
                        )
                    }.toImmutableList()
            }

            val loanRecordTotals = withContext(Dispatchers.Default) {
                calculateLoanRecordTotals(
                    initialLoanAmount = loan.value?.amount ?: 0.0,
                    records = displayLoanRecords.value
                )
            }
            amountPaid.doubleValue = loanRecordTotals.amountPaid
            loanInterestAmountPaid.doubleValue = loanRecordTotals.interestAmountPaid
            loanTotalAmount.doubleValue = loanRecordTotals.totalAmount

            createLoanTransaction.value = getLoanTransactionUseCase(loanId = loan.value!!.id) != null

        }
    }

    private fun calculateLoanRecordTotals(
        initialLoanAmount: Double,
        records: List<DisplayLoanRecord>,
    ): LoanRecordTotals {
        return records.fold(
            LoanRecordTotals(
                amountPaid = 0.0,
                interestAmountPaid = 0.0,
                totalAmount = initialLoanAmount
            )
        ) { totals, displayLoanRecord ->
            val loanRecord = displayLoanRecord.loanRecord
            val convertedAmount = loanRecord.convertedAmount ?: loanRecord.amount
            when {
                loanRecord.loanRecordType == LoanRecordType.INCREASE ->
                    totals.copy(totalAmount = totals.totalAmount + convertedAmount)

                loanRecord.interest ->
                    totals.copy(interestAmountPaid = totals.interestAmountPaid + convertedAmount)

                else ->
                    totals.copy(amountPaid = totals.amountPaid + convertedAmount)
            }
        }
    }

    fun editLoan(loan: Loan, createLoanTransaction: Boolean = false) {
        viewModelScope.launch {

            this@LoanDetailsViewModel.loan.value?.let {
                loanTransactionSyncUseCase.recalculateLoanRecords(
                    originalLoanAccountId = it.accountId,
                    newLoanAccountId = loan.accountId,
                    loanId = loan.id
                )
            }

            loanTransactionSyncUseCase.editAssociatedLoanTransaction(
                loan = loan,
                createLoanTransaction = createLoanTransaction,
                transaction = getLoanTransactionUseCase(loan.id)
            )

            if (updateLoanUseCase(loan)) {
                load(loanId = loan.id)
            }

        }
    }

    private fun deleteLoan() {
        val loan = loan.value ?: return

        viewModelScope.launch {

            loanTransactionSyncUseCase.deleteAssociatedLoanTransactions(loan.id)

            if (deleteLoanUseCase(loan)) {
                _uiEvents.emit(LoanDetailsUiEvent.CloseScreen)
            }

        }
    }

    private fun createLoanRecord(data: CreateLoanRecordData) {
        if (loan.value == null) return
        val loanId = loan.value?.id ?: return
        val localLoan = loan.value!!

        viewModelScope.launch {

            val modifiedData = data.copy(
                convertedAmount = loanRecordTransactionSyncUseCase.calculateConvertedAmount(
                    data = data,
                    loanAccountId = localLoan.accountId
                )
            )

            val loanRecord = createLoanRecordUseCase(
                loanId = loanId,
                data = modifiedData
            )
            if (loanRecord != null) {
                load(loanId = loanId)

                loanRecordTransactionSyncUseCase.createAssociatedLoanRecordTransaction(
                    data = modifiedData,
                    loan = localLoan,
                    loanRecordId = loanRecord.id
                )
            }

        }
    }

    private fun editLoanRecord(editLoanRecordData: EditLoanRecordData) {
        viewModelScope.launch {
            val loanRecord = editLoanRecordData.newLoanRecord

            val localLoan: Loan = loan.value ?: return@launch

            val convertedAmount = loanRecordTransactionSyncUseCase.calculateConvertedAmount(
                loanAccountId = localLoan.accountId,
                newLoanRecord = editLoanRecordData.newLoanRecord,
                originalLoanRecord = editLoanRecordData.originalLoanRecord,
                reCalculateLoanAmount = editLoanRecordData.reCalculateLoanAmount
            )

            val modifiedLoanRecord =
                editLoanRecordData.newLoanRecord.copy(convertedAmount = convertedAmount)

            loanRecordTransactionSyncUseCase.editAssociatedLoanRecordTransaction(
                loan = localLoan,
                createLoanRecordTransaction = editLoanRecordData.createLoanRecordTransaction,
                loanRecord = loanRecord,
            )

            if (updateLoanRecordUseCase(modifiedLoanRecord)) {
                load(loanId = modifiedLoanRecord.loanId)
            }

        }
    }

    private fun deleteLoanRecord(loanRecordId: UUID) {
        val loanId = loan.value?.id ?: return
        val loanRecord = displayLoanRecordById(loanRecordId)?.loanRecord ?: return

        viewModelScope.launch {

            if (deleteLoanRecordUseCase(loanRecord)) {
                load(loanId = loanId)
            }

            loanRecordTransactionSyncUseCase.deleteAssociatedLoanRecordTransaction(loanRecordId = loanRecord.id)

        }
    }

    private fun displayLoanRecordById(loanRecordId: UUID): DisplayLoanRecord? {
        return displayLoanRecords.value.firstOrNull { it.loanRecord.id == loanRecordId }
    }

    private fun handleChangeDate() {
        dateTimePicker.pickDate(
            initialDate = currentLoanRecordDateTime()
        ) { localDate ->

            val localTime = currentLoanRecordDateTime().let {
                it.toLocalTimeInSystemZone()
            }

            updateDateTime(localDate.atTime(localTime))
        }
    }

    private fun handleChangeTime() {
        dateTimePicker.pickTime(
            initialTime = currentLoanRecordDateTime().let {
                it.toLocalTimeInSystemZone()
            }
        ) { localTime ->
            val localDate = currentLoanRecordDateTime().let {
                it.toLocalDateInSystemZone()
            }

            updateDateTime(localDate.atTime(localTime))
        }
    }

    private fun currentLoanRecordDateTime(): Instant {
        return loanRecordModalLoanRecord.value?.dateTime ?: dateTime.value
    }

    private fun updateDateTime(newDateTime: LocalDateTime) {
        val newDateTimeUtc = newDateTime.toUtcInstant()
        if (loanRecordModalVisible.value) {
            loanRecordModalLoanRecord.value = loanRecordModalLoanRecord.value?.copy(
                dateTime = newDateTimeUtc
            )
            dateTime.value = newDateTimeUtc
        }
    }

    private fun handleLoanChangeDate() {
        dateTimePicker.pickDate(
            initialDate = loanModalLoan.value?.dateTime?.let {
                it.toUtcInstant()
            } ?: nowUtc()
        ) { localDate ->

            val localTime = loanModalLoan.value?.dateTime?.let {
                it.toLocalTime()
            } ?: nowLocalTime()

            updateLoanDateTime(localDate.atTime(localTime))
        }
    }

    private fun handleLoanChangeTime() {
        dateTimePicker.pickTime(
            initialTime = loanModalLoan.value?.dateTime?.let {
                it.toLocalTime()
            } ?: nowLocalTime()
        ) { localTime ->
            val localDate = loanModalLoan.value?.dateTime?.let {
                it.toLocalDate()
            } ?: nowLocalDate()

            updateLoanDateTime(localDate.atTime(localTime))
        }
    }

    private fun updateLoanDateTime(newDateTime: LocalDateTime) {
        val newDateTimeUtc = newDateTime.toUtcInstant()
        loanModalLoan.value?.let { currentLoan ->
            loanModalLoan.value = currentLoan.copy(
                dateTime = newDateTime
            )
            dateTime.value = newDateTimeUtc
        }
    }

    private fun createAccount(data: CreateAccountData) {
        viewModelScope.launch {
            createAccountWithBalanceUseCase(data)
            accounts.value = loadAccounts()
        }
    }

    private suspend fun loadAccounts(): ImmutableList<LoanAccount> {
        return getAccountsUseCase()
            .map { it.toLoanAccount() }
            .toImmutableList()
    }

    private fun findAccount(
        accounts: List<LoanAccount>,
        accountId: UUID?,
    ): LoanAccount? {
        return accountId?.let { uuid ->
            accounts.find { acc ->
                acc.id == uuid
            }
        }
    }

    private fun selectedLoanAccountCurrencyCode(): String? {
        val accountId = selectedLoanAccountId.value ?: return null
        return findAccount(accounts.value, accountId)?.currency
    }
}
