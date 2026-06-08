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
import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.data.model.Loan
import com.ivy.data.model.LoanRecord
import com.ivy.loans.data.DisplayLoanRecord
import com.ivy.loans.loandetails.events.DeleteLoanModalEvent
import com.ivy.loans.loandetails.events.LoanDetailsScreenEvent
import com.ivy.loans.loandetails.events.LoanModalEvent
import com.ivy.loans.loandetails.events.LoanRecordModalEvent
import com.ivy.ui.ComposeViewModel
import com.ivy.ui.time.DateTimePicker
import com.ivy.domain.usecase.account.GetLegacyAccountsUseCase
import com.ivy.data.model.CreateAccountData
import com.ivy.data.model.CreateLoanRecordData
import com.ivy.data.model.EditLoanRecordData
import com.ivy.ui.modal.LoanModalData
import com.ivy.ui.modal.LoanRecordModalData
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
    private val getLegacyAccountsUseCase: GetLegacyAccountsUseCase,
    private val getLoanUseCase: GetLoanUseCase,
    private val dateTimePicker: DateTimePicker,
) : ComposeViewModel<LoanDetailsScreenState, LoanDetailsScreenEvent>() {

    private val baseCurrency = mutableStateOf("")
    private val loan = mutableStateOf<Loan?>(null)
    private val displayLoanRecords =
        mutableStateOf<ImmutableList<DisplayLoanRecord>>(persistentListOf())
    private val loanTotalAmount = mutableDoubleStateOf(0.0)
    private val amountPaid = mutableDoubleStateOf(0.0)
    private val accounts = mutableStateOf<ImmutableList<LegacyAccount>>(persistentListOf())
    private val loanInterestAmountPaid = mutableDoubleStateOf(0.0)
    private val selectedLoanAccountId = mutableStateOf<UUID?>(null)
    private val createLoanTransaction = mutableStateOf(false)
    private var defaultCurrencyCode = ""
    private val loanModalData = mutableStateOf<LoanModalData?>(null)
    private val loanRecordModalData = mutableStateOf<LoanRecordModalData?>(null)
    private val waitModalVisible = mutableStateOf(false)
    private val isDeleteModalVisible = mutableStateOf(false)
    private var dateTime = mutableStateOf<Instant>(nowUtc())
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
            selectedLoanAccount = selectedLoanAccount(),
            createLoanTransaction = createLoanTransaction.value,
            loanModalData = loanModalData.value,
            loanRecordModalData = loanRecordModalData.value,
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
                loanRecordModalData.value = LoanRecordModalData(
                    loanRecord = event.displayLoanRecord.loanRecord,
                    baseCurrency = event.displayLoanRecord.loanRecordCurrencyCode,
                    selectedAccount = event.displayLoanRecord.account,
                    createLoanRecordTransaction = event.displayLoanRecord.loanRecordTransaction,
                    isLoanInterest = event.displayLoanRecord.loanRecord.interest,
                    loanAccountCurrencyCode = event.displayLoanRecord.loanCurrencyCode
                )
            }

            is LoanRecordModalEvent.OnCreateLoanRecord -> {
                createLoanRecord(event.loanRecordData)
            }

            is LoanRecordModalEvent.OnDeleteLoanRecord -> {
                deleteLoanRecord(event.loanRecord)
            }

            LoanRecordModalEvent.OnDismissLoanRecord -> {
                loanRecordModalData.value = null
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
                loanModalData.value = null
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
                loanModalData.value = LoanModalData(
                    loan = loan.value,
                    baseCurrency = baseCurrency.value,
                    autoFocusKeyboard = false,
                    autoOpenAmountModal = true,
                    selectedAccount = selectedLoanAccount(),
                    createLoanTransaction = createLoanTransaction.value
                )
            }

            LoanDetailsScreenEvent.OnEditLoanClick -> {
                loanModalData.value = LoanModalData(
                    loan = loan.value,
                    baseCurrency = baseCurrency.value,
                    autoFocusKeyboard = false,
                    selectedAccount = selectedLoanAccount(),
                    createLoanTransaction = createLoanTransaction.value
                )
            }

            LoanDetailsScreenEvent.OnAddRecord -> {
                loanRecordModalData.value = LoanRecordModalData(
                    loanRecord = null,
                    baseCurrency = baseCurrency.value,
                    selectedAccount = selectedLoanAccount()
                )
            }

            is LoanDetailsScreenEvent.OnCreateAccount -> {
                createAccount(event.data)
            }

            else -> {}
        }
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

            accounts.value = getLegacyAccountsUseCase()

            loan.value = getLoanUseCase(loanId)
            selectedLoanAccountId.value = loan.value?.accountId

            loan.value?.let { loan ->
                selectedLoanAccount()?.let { acc ->
                    baseCurrency.value = acc.currency ?: defaultCurrencyCode
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
                            account = account,
                            loanRecordTransaction = hasTransaction,
                            loanRecordCurrencyCode = account?.currency ?: defaultCurrencyCode,
                            loanCurrencyCode = selectedLoanAccount()?.currency
                                ?: defaultCurrencyCode
                        )
                    }.toImmutableList()
            }

            withContext(Dispatchers.Default) {
                // Using a local variable to calculate the amount and then reassigning to
                // the State variable to reduce the amount of compose re-draws
                var amtPaid = 0.0
                var loanInterestAmtPaid = 0.0
                displayLoanRecords.value.forEach {
                    // We do not want to calculate records that increase loan.
                    if (it.loanRecord.loanRecordType == LoanRecordType.INCREASE) {
                        return@forEach
                    }
                    val convertedAmount = it.loanRecord.convertedAmount ?: it.loanRecord.amount
                    if (!it.loanRecord.interest) {
                        amtPaid += convertedAmount
                    } else {
                        loanInterestAmtPaid += convertedAmount
                    }
                }

                amountPaid.doubleValue = amtPaid
                loanInterestAmountPaid.doubleValue = loanInterestAmtPaid
            }

            withContext(Dispatchers.Default) {
                // Calculate total amount of loan borrowed or lent.
                // That is initial amount + each record that increased the loan.
                val totalAmount =
                    displayLoanRecords.value.fold(loan.value?.amount ?: 0.0) { value, record ->
                        if (record.loanRecord.loanRecordType == LoanRecordType.INCREASE) {
                            val convertedAmount =
                                record.loanRecord.convertedAmount ?: record.loanRecord.amount
                            value + convertedAmount
                        } else {
                            value
                        }
                    }
                loanTotalAmount.doubleValue = totalAmount
            }

            createLoanTransaction.value = getLoanTransactionUseCase(loanId = loan.value!!.id) != null

        }
    }

    fun editLoan(loan: Loan, createLoanTransaction: Boolean = false) {
        viewModelScope.launch {

            this@LoanDetailsViewModel.loan.value?.let {
                loanTransactionSyncUseCase.recalculateLoanRecords(
                    oldLoanAccountId = it.accountId,
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
                oldLoanRecord = editLoanRecordData.originalLoanRecord,
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

    private fun deleteLoanRecord(loanRecord: LoanRecord) {
        val loanId = loan.value?.id ?: return

        viewModelScope.launch {

            if (deleteLoanRecordUseCase(loanRecord)) {
                load(loanId = loanId)
            }

            loanRecordTransactionSyncUseCase.deleteAssociatedLoanRecordTransaction(loanRecordId = loanRecord.id)

        }
    }

    private fun handleChangeDate() {
        dateTimePicker.pickDate(
            initialDate = loanRecordModalData.value?.loanRecord?.dateTime ?: nowUtc()
        ) { localDate ->

            val localTime = loanRecordModalData.value?.loanRecord?.dateTime?.let {
                it.toLocalTimeInSystemZone()
            } ?: nowLocalTime()

            updateDateTime(localDate.atTime(localTime))
        }
    }

    private fun handleChangeTime() {
        dateTimePicker.pickTime(
            initialTime = loanRecordModalData.value?.loanRecord?.dateTime?.let {
                it.toLocalTimeInSystemZone()
            } ?: nowLocalTime()
        ) { localTime ->
            val localDate = loanRecordModalData.value?.loanRecord?.dateTime?.let {
                it.toLocalDateInSystemZone()
            } ?: nowLocalDate()

            updateDateTime(localDate.atTime(localTime))
        }
    }

    private fun updateDateTime(newDateTime: LocalDateTime) {
        val newDateTimeUtc = newDateTime.toUtcInstant()
        loanRecordModalData.value?.let { currentData ->
            loanRecordModalData.value = currentData.copy(
                loanRecord = currentData.loanRecord?.copy(
                    dateTime = newDateTimeUtc
                )
            )
            dateTime.value = newDateTimeUtc
        }
    }

    private fun handleLoanChangeDate() {
        dateTimePicker.pickDate(
            initialDate = loanModalData.value?.loan?.dateTime?.let {
                it.toUtcInstant()
            } ?: nowUtc()
        ) { localDate ->

            val localTime = loanModalData.value?.loan?.dateTime?.let {
                it.toLocalTime()
            } ?: nowLocalTime()

            updateLoanDateTime(localDate.atTime(localTime))
        }
    }

    private fun handleLoanChangeTime() {
        dateTimePicker.pickTime(
            initialTime = loanModalData.value?.loan?.dateTime?.let {
                it.toLocalTime()
            } ?: nowLocalTime()
        ) { localTime ->
            val localDate = loanModalData.value?.loan?.dateTime?.let {
                it.toLocalDate()
            } ?: nowLocalDate()

            updateLoanDateTime(localDate.atTime(localTime))
        }
    }

    private fun updateLoanDateTime(newDateTime: LocalDateTime) {
        val newDateTimeUtc = newDateTime.toUtcInstant()
        loanModalData.value?.let { currentData ->
            loanModalData.value = currentData.copy(
                loan = currentData.loan?.copy(
                    dateTime = newDateTime
                )
            )
            dateTime.value = newDateTimeUtc
        }
    }

    fun onLoanTransactionChecked(boolean: Boolean) {
        createLoanTransaction.value = boolean
    }

    private fun createAccount(data: CreateAccountData) {
        viewModelScope.launch {
            createAccountWithBalanceUseCase(data)
            accounts.value = getLegacyAccountsUseCase()
        }
    }

    private fun findAccount(
        accounts: List<LegacyAccount>,
        accountId: UUID?,
    ): LegacyAccount? {
        return accountId?.let { uuid ->
            accounts.find { acc ->
                acc.id == uuid
            }
        }
    }

    private fun selectedLoanAccount(): LegacyAccount? {
        val accountId = selectedLoanAccountId.value ?: return null
        return findAccount(accounts.value, accountId)
    }
}
