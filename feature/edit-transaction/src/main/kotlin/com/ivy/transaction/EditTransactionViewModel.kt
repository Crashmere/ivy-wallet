package com.ivy.transaction

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.ivy.data.model.legacy.LegacyTransaction
import com.ivy.data.model.TransactionType
import com.ivy.ui.resource.ResourceProvider
import com.ivy.data.model.Category
import com.ivy.data.model.CategoryId
import com.ivy.data.model.Tag
import com.ivy.data.model.TagId
import com.ivy.data.model.TransactionId
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.domain.preferences.toggles.PreferenceToggleService
import com.ivy.domain.preferences.toggles.PreferenceToggleCatalog
import com.ivy.domain.usecase.category.GetCategoriesUseCase
import com.ivy.domain.usecase.category.GetCategoryUseCase
import com.ivy.domain.usecase.category.CreateCategoryUseCase
import com.ivy.domain.usecase.category.UpdateCategoryUseCase
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.domain.usecase.loan.GetLoanUseCase
import com.ivy.domain.usecase.planned.PayOrSkipLegacyPlannedTransactionUseCase
import com.ivy.domain.usecase.tag.AssociateTagToTransactionUseCase
import com.ivy.domain.usecase.tag.CopyTagsToTransactionUseCase
import com.ivy.domain.usecase.tag.CreateTagUseCase
import com.ivy.domain.usecase.tag.DeleteTagUseCase
import com.ivy.domain.usecase.tag.GetTransactionTagIdsUseCase
import com.ivy.domain.usecase.tag.GetTagsUseCase
import com.ivy.ui.platform.Toaster
import com.ivy.ui.preferences.asEnabledState
import com.ivy.domain.usecase.tag.RemoveTagFromTransactionUseCase
import com.ivy.domain.usecase.tag.SaveTagUseCase
import com.ivy.domain.usecase.tag.SearchTagsUseCase
import com.ivy.domain.usecase.account.CreateAccountWithBalanceUseCase
import com.ivy.domain.usecase.account.GetLastSelectedAccountIdUseCase
import com.ivy.domain.usecase.transaction.DeleteTransactionUseCase
import com.ivy.domain.usecase.transaction.GetLegacyTransactionUseCase
import com.ivy.domain.usecase.transaction.SaveLegacyTransactionUseCase
import com.ivy.domain.usecase.transaction.SuggestTransactionTitlesUseCase
import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.ui.ComposeViewModel
import com.ivy.ui.R
import com.ivy.ui.time.DateTimePicker
import com.ivy.domain.usecase.account.GetLegacyAccountUseCase
import com.ivy.domain.usecase.account.GetLegacyAccountsUseCase
import com.ivy.domain.usecase.account.SetLastSelectedAccountIdUseCase
import com.ivy.domain.usecase.exchange.LegacyExchangeRatesUseCase
import com.ivy.domain.usecase.loan.UpdateAssociatedLoanDataUseCase
import com.ivy.data.model.CreateAccountData
import com.ivy.data.model.CreateCategoryData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@Suppress("LargeClass")
@Stable
@HiltViewModel
internal class EditTransactionViewModel @Inject internal constructor(
    private val resourceProvider: ResourceProvider,
    private val toaster: Toaster,
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val getCategoryUseCase: GetCategoryUseCase,
    private val getLoanUseCase: GetLoanUseCase,
    private val getLastSelectedAccountId: GetLastSelectedAccountIdUseCase,
    private val setLastSelectedAccountId: SetLastSelectedAccountIdUseCase,
    private val exchangeRatesUseCase: LegacyExchangeRatesUseCase,
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val createAccountWithBalanceUseCase: CreateAccountWithBalanceUseCase,
    private val payOrSkipLegacyPlannedTransactionUseCase: PayOrSkipLegacyPlannedTransactionUseCase,
    private val suggestTransactionTitlesUseCase: SuggestTransactionTitlesUseCase,
    private val updateAssociatedLoanDataUseCase: UpdateAssociatedLoanDataUseCase,
    private val getLegacyAccountsUseCase: GetLegacyAccountsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getLegacyTransactionUseCase: GetLegacyTransactionUseCase,
    private val getLegacyAccountUseCase: GetLegacyAccountUseCase,
    private val saveLegacyTransactionUseCase: SaveLegacyTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val getTransactionTagIdsUseCase: GetTransactionTagIdsUseCase,
    private val createTagUseCase: CreateTagUseCase,
    private val saveTagUseCase: SaveTagUseCase,
    private val deleteTagUseCase: DeleteTagUseCase,
    private val associateTagToTransactionUseCase: AssociateTagToTransactionUseCase,
    private val removeTagFromTransactionUseCase: RemoveTagFromTransactionUseCase,
    private val copyTagsToTransactionUseCase: CopyTagsToTransactionUseCase,
    private val getTagsUseCase: GetTagsUseCase,
    private val searchTagsUseCase: SearchTagsUseCase,
    private val preferenceToggles: PreferenceToggleCatalog,
    private val preferenceToggleService: PreferenceToggleService,
    private val dateTimePicker: DateTimePicker,
) : ComposeViewModel<EditTransactionViewState, EditTransactionViewEvent>() {

    private var transactionType by mutableStateOf(TransactionType.EXPENSE)
    private var initialTitle by mutableStateOf<String?>(null)
    private var titleSuggestions by mutableStateOf(persistentSetOf<String>())
    private var currency by mutableStateOf("")
    private var description by mutableStateOf<String?>(null)
    private var dateTime by mutableStateOf<Instant?>(null)
    private var dueDate by mutableStateOf<Instant?>(null)
    private var accounts by mutableStateOf<ImmutableList<LegacyAccount>>(persistentListOf())
    private var categories by mutableStateOf<ImmutableList<Category>>(persistentListOf())
    private var tags by mutableStateOf<ImmutableList<Tag>>(persistentListOf())
    private var transactionAssociatedTags by mutableStateOf<ImmutableList<TagId>>(persistentListOf())
    private var account by mutableStateOf<LegacyAccount?>(null)
    private var toAccount by mutableStateOf<LegacyAccount?>(null)
    private var category by mutableStateOf<Category?>(null)
    private var amount by mutableDoubleStateOf(0.0)
    private var hasChanges by mutableStateOf(false)
    private var displayLoanHelper by mutableStateOf(EditTransactionDisplayLoan())

    private var paidHistory: Instant? = null

    // This is used to when the transaction is associated with a loan/loan record,
    // used to indicate the background updating of loan/loanRecord data
    private var backgroundProcessingStarted by mutableStateOf(false)

    private var customExchangeRateState by mutableStateOf(CustomExchangeRateState())

    private var loadedTransaction: LegacyTransaction? = null
    private var editMode = false

    // Used for optimising in updating all loan/loanRecords
    private var accountsChanged = false

    private var title: String? = null
    private lateinit var baseUserCurrency: String
    private var tagSearchJob: Job? = null
    private val tagSearchDebounceTimeInMillis: Long = 500
    private val _uiEvents = MutableSharedFlow<EditTransactionUiEvent>()
    val uiEvents: SharedFlow<EditTransactionUiEvent> = _uiEvents.asSharedFlow()

    fun start(
        initialTransactionId: UUID?,
        type: TransactionType,
        accountId: UUID?,
        categoryId: UUID?,
    ) {
        viewModelScope.launch {
            editMode = initialTransactionId != null

            baseUserCurrency = baseCurrency()

            val tagList = async { getAllTags() }

            val getAccounts = getLegacyAccountsUseCase()
            if (getAccounts.isEmpty()) {
                closeScreen()
                return@launch
            }
            accounts = getAccounts

            categories = sortCategories()

            reset()

            loadedTransaction = initialTransactionId?.let {
                getLegacyTransactionUseCase(it)
            } ?: LegacyTransaction(
                accountId = defaultAccountId(
                    accountId = accountId,
                    accounts = getAccounts
                ),
                categoryId = categoryId,
                type = type,
                amount = BigDecimal.ZERO,
                toAmount = BigDecimal.ZERO
            )

            tags = tagList.await()
            transactionAssociatedTags =
                getTransactionTagIdsUseCase(loadedTransaction().id)
                    .toImmutableList()
            display(loadedTransaction!!)
        }
    }

    @Composable
    override fun uiState(): EditTransactionViewState {
        return EditTransactionViewState(
            transactionType = getTransactionType(),
            initialTitle = getInitialTitle(),
            titleSuggestions = getTitleSuggestions(),
            currency = getCurrency(),
            description = getDescription(),
            dateTime = getDateTime(),
            dueDate = getDueDate(),
            accounts = getAccounts(),
            categories = getCategories(),
            account = getAccount(),
            toAccount = getToAccount(),
            category = getCategory(),
            amount = getAmount(),
            hasChanges = getHasChanges(),
            displayLoanHelper = getDisplayLoanHelper(),
            backgroundProcessingStarted = getBackgroundProcessingStarted(),
            customExchangeRateState = getCustomExchangeRateState(),
            tags = getTags(),
            transactionAssociatedTags = getTransactionAssociatedTags()
        )
    }

    @Composable
    private fun getTransactionType(): TransactionType {
        return transactionType
    }

    @Composable
    private fun getInitialTitle(): String? {
        return initialTitle
    }

    @Composable
    private fun getTitleSuggestions(): ImmutableSet<String> {
        val preference = preferenceToggles.showTitleSuggestions
        return if (
            preferenceToggleService.enabledFlow(preference)
                .asEnabledState(preference.defaultValue)
        ) {
            titleSuggestions
        } else {
            persistentSetOf()
        }
    }

    @Composable
    private fun getCurrency(): String {
        return currency
    }

    @Composable
    private fun getDescription(): String? {
        return description
    }

    @Composable
    private fun getDateTime(): Instant? {
        return dateTime
    }

    @Composable
    private fun getDueDate(): Instant? {
        return dueDate
    }

    @Composable
    private fun getAccounts(): ImmutableList<LegacyAccount> {
        return accounts
    }

    @Composable
    private fun getCategories(): ImmutableList<Category> {
        return categories
    }

    @Composable
    private fun getAccount(): LegacyAccount? {
        return account
    }

    @Composable
    private fun getToAccount(): LegacyAccount? {
        return toAccount
    }

    @Composable
    private fun getCategory(): Category? {
        return category
    }

    @Composable
    private fun getAmount(): Double {
        return amount
    }

    @Composable
    private fun getHasChanges(): Boolean {
        return hasChanges
    }

    @Composable
    private fun getDisplayLoanHelper(): EditTransactionDisplayLoan {
        return displayLoanHelper
    }

    @Composable
    private fun getBackgroundProcessingStarted(): Boolean {
        return backgroundProcessingStarted
    }

    @Composable
    private fun getCustomExchangeRateState(): CustomExchangeRateState {
        return customExchangeRateState
    }

    @Composable
    private fun getTags(): ImmutableList<Tag> {
        return tags
    }

    @Composable
    private fun getTransactionAssociatedTags(): ImmutableList<TagId> {
        return transactionAssociatedTags
    }

    @Suppress("CyclomaticComplexMethod")
    override fun onEvent(event: EditTransactionViewEvent) {
        when (event) {
            is EditTransactionViewEvent.CreateAccount -> createAccount(event.data)
            is EditTransactionViewEvent.CreateCategory -> createCategory(event.data)
            EditTransactionViewEvent.Delete -> delete()
            EditTransactionViewEvent.Duplicate -> duplicate()
            is EditTransactionViewEvent.EditCategory -> editCategory(event.updatedCategory)
            is EditTransactionViewEvent.OnAccountChanged -> onAccountChanged(event.newAccount)
            is EditTransactionViewEvent.OnAmountChanged -> onAmountChanged(event.newAmount)
            is EditTransactionViewEvent.OnCategoryChanged -> onCategoryChanged(event.newCategory)
            is EditTransactionViewEvent.OnDescriptionChanged ->
                onDescriptionChanged(event.newDescription)

            is EditTransactionViewEvent.OnDueDateChanged -> onDueDateChanged(event.newDueDate)
            EditTransactionViewEvent.OnPayPlannedPayment -> onPayPlannedPayment()
            is EditTransactionViewEvent.OnChangeDate -> handleChangeDate()
            is EditTransactionViewEvent.OnChangeTime -> handleChangeTime()
            is EditTransactionViewEvent.OnSetTransactionType ->
                onSetTransactionType(event.newTransactionType)

            is EditTransactionViewEvent.OnTitleChanged -> onTitleChanged(event.newTitle)
            is EditTransactionViewEvent.OnToAccountChanged -> onToAccountChanged(event.newAccount)
            is EditTransactionViewEvent.Save -> save(event.closeScreen)
            is EditTransactionViewEvent.SetHasChanges -> setHasChanges(event.hasChangesValue)
            is EditTransactionViewEvent.UpdateExchangeRate -> updateExchangeRate(event.exRate)
            is EditTransactionViewEvent.TagEvent -> handleTagEvent(event)
        }
    }

    private fun handleTagEvent(event: EditTransactionViewEvent.TagEvent) {
        when (event) {
            is EditTransactionViewEvent.TagEvent.SaveTag -> onTagSaved(event.name)
            is EditTransactionViewEvent.TagEvent.OnTagSelect -> associateTagToTransaction(event.selectedTag)
            is EditTransactionViewEvent.TagEvent.OnTagDeSelect -> removeTagAssociation(event.selectedTag)
            is EditTransactionViewEvent.TagEvent.OnTagSearch -> searchTag(event.query)
            is EditTransactionViewEvent.TagEvent.OnTagDelete -> deleteTag(event.selectedTag)
            is EditTransactionViewEvent.TagEvent.OnTagEdit -> updateTagInformation(event.newTag)
        }
    }

    private suspend fun defaultAccountId(
        accountId: UUID?,
        accounts: List<LegacyAccount>,
    ): UUID {
        if (accountId != null) {
            return accountId
        }

        val lastSelectedId = getLastSelectedAccountId()
        if (lastSelectedId != null && withContext(Dispatchers.IO) {
                accounts.find {
                    it.id == lastSelectedId
                }
            } != null
        ) {
            // use last selected account
            return lastSelectedId
        }

        return accounts.first().id
    }

    private suspend fun display(transaction: LegacyTransaction) {
        this.title = transaction.title

        transactionType = transaction.type
        initialTitle = transaction.title
        dateTime = transaction.dateTime
        description = transaction.description
        dueDate = transaction.dueDate
        paidHistory = transaction.paidFor
        val selectedAccount = getLegacyAccountUseCase(transaction.accountId)!!
        account = selectedAccount
        toAccount = transaction.toAccountId?.let {
            getLegacyAccountUseCase(it)
        }
        category = transaction.categoryId?.let {
            getCategoryUseCase(CategoryId(it))
        }
        amount = transaction.amount.toDouble()

        updateCurrency(account = selectedAccount)

        customExchangeRateState = if (transaction.toAccountId == null) {
            CustomExchangeRateState()
        } else {
            val exchangeRate = transaction.toAmount / transaction.amount
            val toAccountCurrency =
                accounts.find { acc -> acc.id == transaction.toAccountId }?.currency
            CustomExchangeRateState(
                showCard = toAccountCurrency != account?.currency,
                exchangeRate = exchangeRate.toDouble(),
                convertedAmount = transaction.toAmount.toDouble(),
                toCurrencyCode = toAccountCurrency,
                fromCurrencyCode = currency
            )
        }

        displayLoanHelper = getDisplayLoanHelper(transaction = transaction)
    }

    private suspend fun getDisplayLoanHelper(transaction: LegacyTransaction): EditTransactionDisplayLoan {
        if (transaction.loanId == null) {
            return EditTransactionDisplayLoan()
        }

        val loan =
            getLoanUseCase(transaction.loanId!!) ?: return EditTransactionDisplayLoan()
        val isLoanRecord = transaction.loanRecordId != null

        val loanWarningDescription = if (isLoanRecord) {
            resourceProvider.getString(
                R.string.note_transaction_associated_with_loan_record_of_loan,
                loan.name
            )
        } else {
            resourceProvider.getString(
                R.string.note_you_are_trying_to_change_the_account_associated_with_the_loan,
                loan.name
            )
        }

        val loanCaption =
            if (isLoanRecord) {
                resourceProvider.getString(
                    R.string.this_transaction_is_associated_with_loan_record,
                    loan.name
                )
            } else {
                resourceProvider.getString(
                    R.string.this_transaction_is_associated_with_loan,
                    loan.name
                )
            }

        return EditTransactionDisplayLoan(
            isLoan = true,
            isLoanRecord = isLoanRecord,
            loanCaption = loanCaption,
            loanWarningDescription = loanWarningDescription
        )
    }

    private fun onAmountChanged(newAmount: Double) {
        viewModelScope.launch {
            loadedTransaction = loadedTransaction().copy(
                amount = newAmount.toBigDecimal()
            )
            amount = newAmount
            updateCustomExchangeRateState(amt = newAmount)

            saveIfEditMode()
        }
    }

    private fun onTitleChanged(newTitle: String?) {
        loadedTransaction = loadedTransaction().copy(
            title = newTitle
        )
        this.title = newTitle

        saveIfEditMode()

        updateTitleSuggestions(newTitle)
    }

    private fun onDescriptionChanged(newDescription: String?) {
        loadedTransaction = loadedTransaction().copy(
            description = newDescription
        )
        description = newDescription

        saveIfEditMode()
    }

    private fun onAccountChanged(newAccount: LegacyAccount) {
        viewModelScope.launch {
            loadedTransaction = loadedTransaction().copy(
                accountId = newAccount.id
            )
            account = newAccount

            updateCustomExchangeRateState(fromAccount = newAccount)

            viewModelScope.launch {
                updateCurrency(account = newAccount)
            }

            accountsChanged = true

            // update last selected account
            setLastSelectedAccountId(newAccount.id)

            saveIfEditMode()

            updateTitleSuggestions()
        }
    }

    private suspend fun updateCurrency(account: LegacyAccount) {
        currency = account.currency ?: baseCurrency()
    }

    private fun onToAccountChanged(newAccount: LegacyAccount) {
        viewModelScope.launch {
            loadedTransaction = loadedTransaction().copy(
                toAccountId = newAccount.id
            )
            toAccount = newAccount
            updateCustomExchangeRateState(toAccountValue = newAccount)

            saveIfEditMode()
        }
    }

    private fun onDueDateChanged(newDueDate: LocalDateTime?) {
        val newDueDateUtc = newDueDate?.toUtcInstant()
        loadedTransaction = loadedTransaction().copy(
            dueDate = newDueDateUtc
        )
        dueDate = newDueDateUtc

        saveIfEditMode()
    }

    private fun handleChangeDate() {
        dateTimePicker.pickDate(
            initialDate = loadedTransaction?.dateTime,
        ) { localDate ->
            val localTime = loadedTransaction().dateTime?.let {
                it.toLocalTimeInSystemZone()
            } ?: LocalTime.now()
            loadedTransaction = loadedTransaction().copy(
                date = localDate,
            )
            updateDateTime(localDate.atTime(localTime))
        }
    }

    private fun handleChangeTime() {
        dateTimePicker.pickTime(
            initialTime = loadedTransaction?.dateTime?.let {
                it.toLocalTimeInSystemZone()
            }
        ) { localTime ->
            val localDate = loadedTransaction().dateTime?.let {
                it.toLocalDateInSystemZone()
            } ?: LocalDate.now()
            loadedTransaction = loadedTransaction().copy(
                time = localTime,
            )
            updateDateTime(localDate.atTime(localTime))
        }
    }

    private fun updateDateTime(newDateTime: LocalDateTime) {
        val newDateTimeUtc = newDateTime.toUtcInstant()
        loadedTransaction = loadedTransaction().copy(
            dateTime = newDateTimeUtc,
        )
        dateTime = newDateTimeUtc

        saveIfEditMode()
    }

    private fun onSetTransactionType(newTransactionType: TransactionType) {
        loadedTransaction = loadedTransaction().copy(
            type = newTransactionType
        )
        transactionType = newTransactionType
        saveIfEditMode()
    }

    private fun onPayPlannedPayment() {
        viewModelScope.launch {
            payOrSkipLegacyPlannedTransactionUseCase(
                transaction = loadedTransaction()
            )?.let { paidTransaction ->
                loadedTransaction = paidTransaction
                paidHistory = paidTransaction.paidFor
                dueDate = paidTransaction.dueDate
                dateTime = paidTransaction.dateTime

                saveIfEditMode(
                    closeScreen = true
                )
            }
        }
    }

    private fun delete() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                loadedTransaction?.let {
                    deleteTransactionUseCase(TransactionId(it.id))
                }
            }
            closeScreen()
        }
    }

    private fun duplicate() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val id = UUID.randomUUID()
                loadedTransaction()
                    .copy(
                        id = id,
                        dateTime = Instant.now(),
                    )
                    .let { saveLegacyTransactionUseCase(it) }

                copyTagsToTransactionUseCase(transactionAssociatedTags, id)

            }

            closeScreen()
        }
    }

    private fun createCategory(data: CreateCategoryData) {
        viewModelScope.launch {
            createCategoryUseCase(data)?.let {
                categories = sortCategories()

                // Select the newly created category
                onCategoryChanged(it)
            }
        }
    }

    private fun onCategoryChanged(newCategory: Category?) {
        loadedTransaction = loadedTransaction().copy(
            categoryId = newCategory?.id?.value
        )
        category = newCategory

        saveIfEditMode()

        updateTitleSuggestions()
    }

    private fun updateTitleSuggestions(title: String? = loadedTransaction().title) {
        viewModelScope.launch {
            titleSuggestions = withContext(Dispatchers.IO) {
                suggestTransactionTitlesUseCase(
                    title = title,
                    categoryId = category?.id?.value,
                    accountId = account?.id
                )
            }.toPersistentSet()
        }
    }

    private fun editCategory(updatedCategory: Category) {
        viewModelScope.launch {
            if (updateCategoryUseCase(updatedCategory)) {
                categories = sortCategories()
            }
        }
    }

    private fun createAccount(data: CreateAccountData) {
        viewModelScope.launch {
            createAccountWithBalanceUseCase(data)
            accounts = getLegacyAccountsUseCase()
        }
    }

    private fun save(closeScreen: Boolean = true) {
        if (!validTransaction()) {
            return
        }

        viewModelScope.launch {
            saveInternal(closeScreen = closeScreen)
        }
    }

    private suspend fun saveInternal(closeScreen: Boolean) {
        try {
            withContext(Dispatchers.IO) {
                val amount = amount.toBigDecimal()

                loadedTransaction = loadedTransaction().copy(
                    accountId = account?.id ?: error("no accountId"),
                    toAccountId = toAccount?.id,
                    toAmount = customExchangeRateState.convertedAmount?.toBigDecimal()
                        ?: amount,
                    title = title?.trim(),
                    description = description?.trim(),
                    amount = amount,
                    type = transactionType,
                    dueDate = dueDate,
                    paidFor = paidHistory,
                    dateTime = when {
                        loadedTransaction().dateTime == null &&
                                dueDate == null -> {
                            Instant.now()
                        }

                        else -> loadedTransaction().dateTime
                    },
                    categoryId = category?.id?.value
                )

                if (loadedTransaction?.loanId != null) {
                    updateAssociatedLoanDataUseCase(
                        loadedTransaction!!.copy(),
                        onBackgroundProcessingStart = {
                            backgroundProcessingStarted = true
                        },
                        onBackgroundProcessingEnd = {
                            backgroundProcessingStarted = false
                        },
                        accountsChanged = accountsChanged
                    )

                    // Reset Counter
                    accountsChanged = false
                }

                saveLegacyTransactionUseCase(loadedTransaction())

            }

            if (closeScreen) {
                closeScreen()
            }
        } catch (_: Exception) {
        }
    }

    @JvmName("setHasChangesMethod")
    private fun setHasChanges(hasChangesValue: Boolean) {
        hasChanges = hasChangesValue
    }

    private suspend fun transferToAmount(
        amount: Double
    ): Double? {
        if (transactionType != TransactionType.TRANSFER) return null
        val toCurrency = toAccount?.currency ?: baseCurrency()
        val fromCurrency = account?.currency ?: baseCurrency()

        return exchangeRatesUseCase.convertAmount(
            baseCurrency = baseCurrency(),
            amount = amount,
            fromCurrency = fromCurrency,
            toCurrency = toCurrency
        )
    }

    private suspend fun baseCurrency(): String = getBaseCurrencyCode()

    private suspend fun closeScreen() {
        _uiEvents.emit(EditTransactionUiEvent.CloseScreen)
    }

    @Suppress("ReturnCount")
    private fun validTransaction(): Boolean {
        if (hasChosenSameSourceAndDestinationAccountToTransfer()) {
            viewModelScope.launch {
                toaster.show(R.string.msg_source_account_destination_account_same_for_transfer)
            }
            return false
        }
        if (hasNotChosenAccountToTransfer()) {
            viewModelScope.launch {
                toaster.show(R.string.msg_select_account_to_transfer)
            }
            return false
        }

        if (amount == 0.0) {
            return false
        }

        return true
    }

    private fun hasNotChosenAccountToTransfer(): Boolean {
        return transactionType == TransactionType.TRANSFER && toAccount == null
    }

    private fun hasChosenSameSourceAndDestinationAccountToTransfer(): Boolean {
        return transactionType == TransactionType.TRANSFER && toAccount == account
    }

    private fun reset() {
        loadedTransaction = null

        initialTitle = null
        description = null
        dueDate = null
        category = null
        hasChanges = false
    }

    private fun loadedTransaction() = loadedTransaction ?: error("Loaded transaction is null")

    private fun updateExchangeRate(exRate: Double?) {
        viewModelScope.launch {
            updateCustomExchangeRateState(exchangeRate = exRate, resetRate = exRate == null)
        }
    }

    private suspend fun updateCustomExchangeRateState(
        toAccountValue: LegacyAccount? = null,
        fromAccount: LegacyAccount? = null,
        amt: Double? = null,
        exchangeRate: Double? = null,
        resetRate: Boolean = false
    ) {
        withContext(Dispatchers.Default) computation@ {
            val toAcc = toAccountValue ?: toAccount
            val fromAcc = fromAccount ?: account

            val toAccCurrencyCode = toAcc?.currency ?: baseUserCurrency
            val fromAccCurrencyCode = fromAcc?.currency ?: baseUserCurrency

            if (toAcc == null || fromAcc == null || (toAccCurrencyCode == fromAccCurrencyCode)) {
                customExchangeRateState = CustomExchangeRateState()
                return@computation
            }

            val exRate = exchangeRate
                ?: if (isCustomExchangeRateCurrencyCodeMatchingWithSourceAndDestinationAccountCurrencyCode(
                        toAccCurrencyCode = toAccCurrencyCode,
                        fromAccCurrencyCode = fromAccCurrencyCode
                    ) && !resetRate
                ) {
                    customExchangeRateState.exchangeRate
                } else {
                    exchangeRatesUseCase.convertAmount(
                        baseCurrency = baseUserCurrency,
                        amount = 1.0,
                        fromCurrency = fromAccCurrencyCode,
                        toCurrency = toAccCurrencyCode
                    )
                }

            val amount = amt ?: amount

            val customTransferExchangeRateState = CustomExchangeRateState(
                showCard = true,
                toCurrencyCode = toAccCurrencyCode,
                fromCurrencyCode = fromAccCurrencyCode,
                exchangeRate = exRate,
                convertedAmount = exRate * amount
            )

            customExchangeRateState = customTransferExchangeRateState
            withContext(Dispatchers.Main) {
                saveIfEditMode()
            }
        }
    }

    private fun isCustomExchangeRateCurrencyCodeMatchingWithSourceAndDestinationAccountCurrencyCode(
        toAccCurrencyCode: String,
        fromAccCurrencyCode: String
    ): Boolean {
        return customExchangeRateState.showCard &&
                toAccCurrencyCode == customExchangeRateState.toCurrencyCode &&
                fromAccCurrencyCode == customExchangeRateState.fromCurrencyCode
    }

    private fun saveIfEditMode(closeScreen: Boolean = false) {
        if (editMode) {
            hasChanges = true

            save(closeScreen)
        }
    }

    private suspend fun getAllTags(): ImmutableList<Tag> =
        getTagsUseCase().toImmutableList()

    private fun onTagSaved(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            NotBlankTrimmedString.from(name.lowercase(Locale.getDefault()))
                .onRight {
                    createTagUseCase(it)
                    this@EditTransactionViewModel.tags = getAllTags()
                }

            saveIfEditMode()
        }
    }

    private fun associateTagToTransaction(selectedTag: Tag) {
        viewModelScope.launch(Dispatchers.IO) {
            associateTagToTransactionUseCase(loadedTransaction().id, selectedTag.id)
            transactionAssociatedTags =
                getTransactionTagIdsUseCase(loadedTransaction().id).toImmutableList()
        }
    }

    private fun removeTagAssociation(selectedTag: Tag) {
        viewModelScope.launch(Dispatchers.IO) {
            removeTagFromTransactionUseCase(loadedTransaction().id, selectedTag.id)
            transactionAssociatedTags =
                getTransactionTagIdsUseCase(loadedTransaction().id).toImmutableList()
        }
    }

    private fun searchTag(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            tagSearchJob?.cancelAndJoin()
            delay(tagSearchDebounceTimeInMillis)
            tagSearchJob = launch(Dispatchers.IO) {
                NotBlankTrimmedString.from(query.lowercase(Locale.getDefault()))
                    .onRight {
                        tags =
                            searchTagsUseCase(it).toImmutableList()
                    }
                    .onLeft {
                        tags = getTagsUseCase().toImmutableList()
                    }
            }
        }
    }

    private fun deleteTag(selectedTag: Tag) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteTagUseCase(selectedTag.id)
            tags = getAllTags()
        }
    }

    private fun updateTagInformation(newTag: Tag) {
        viewModelScope.launch(Dispatchers.IO) {
            saveTagUseCase(newTag)
            tags = getAllTags()
        }
    }

    private suspend fun sortCategories(): ImmutableList<Category> {
        val categories = getCategoriesUseCase()
        return if (shouldSortCategoriesAscending()) {
            categories.sortedBy { it.name.value }.toImmutableList()
        } else {
            categories.toImmutableList()
        }
    }

    private suspend fun shouldSortCategoriesAscending(): Boolean {
        return preferenceToggleService.isEnabled(preferenceToggles.sortCategoriesAscending)
    }
}

private fun LocalDateTime.toUtcInstant(): Instant =
    atZone(ZoneId.systemDefault()).toInstant()

private fun Instant.toLocalDateInSystemZone(): LocalDate =
    atZone(ZoneId.systemDefault()).toLocalDate()

private fun Instant.toLocalTimeInSystemZone(): LocalTime =
    atZone(ZoneId.systemDefault()).toLocalTime()
