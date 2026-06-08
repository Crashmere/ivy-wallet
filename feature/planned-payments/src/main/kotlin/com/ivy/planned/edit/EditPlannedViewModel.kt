package com.ivy.planned.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.ivy.data.model.TransactionType
import com.ivy.data.model.Category
import com.ivy.data.model.CategoryId
import com.ivy.data.model.IntervalType
import com.ivy.domain.usecase.category.GetCategoriesUseCase
import com.ivy.domain.usecase.category.GetCategoryUseCase
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.domain.usecase.planned.DeletePlannedPaymentRuleUseCase
import com.ivy.domain.usecase.planned.GetPlannedPaymentRuleUseCase
import com.ivy.domain.usecase.planned.SavePlannedPaymentRuleUseCase
import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.data.model.PlannedPaymentRule
import com.ivy.ui.ComposeViewModel
import com.ivy.domain.usecase.account.CreateAccountWithBalanceUseCase
import com.ivy.domain.usecase.account.GetLegacyAccountsUseCase
import com.ivy.domain.usecase.category.CreateCategoryUseCase
import com.ivy.domain.usecase.category.UpdateCategoryUseCase
import com.ivy.data.model.CreateAccountData
import com.ivy.data.model.CreateCategoryData
import com.ivy.legacy.ui.modal.AccountModalData
import com.ivy.legacy.ui.modal.RecurringRuleModalData
import com.ivy.legacy.ui.modal.CategoryModalData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

@Stable
@HiltViewModel
internal class EditPlannedViewModel @Inject internal constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getCategoryUseCase: GetCategoryUseCase,
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val getPlannedPaymentRuleUseCase: GetPlannedPaymentRuleUseCase,
    private val savePlannedPaymentRuleUseCase: SavePlannedPaymentRuleUseCase,
    private val deletePlannedPaymentRuleUseCase: DeletePlannedPaymentRuleUseCase,
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val createAccountWithBalanceUseCase: CreateAccountWithBalanceUseCase,
    private val getLegacyAccountsUseCase: GetLegacyAccountsUseCase,
) : ComposeViewModel<EditPlannedScreenState, EditPlannedScreenEvent>() {

    private var transactionType by mutableStateOf(TransactionType.INCOME)
    private var startDate by mutableStateOf<LocalDateTime?>(null)
    private var intervalN by mutableStateOf<Int?>(null)
    private var intervalType by mutableStateOf<IntervalType?>(null)
    private var oneTime by mutableStateOf(false)
    private var initialTitle by mutableStateOf<String?>(null)
    private var description by mutableStateOf<String?>(null)
    private var accountId by mutableStateOf<UUID?>(null)
    private var category by mutableStateOf<Category?>(null)
    private var amount by mutableDoubleStateOf(0.0)
    private var currency by mutableStateOf("")
    private var categories by mutableStateOf<ImmutableList<Category>>(persistentListOf())
    private var accounts by mutableStateOf<ImmutableList<LegacyAccount>>(persistentListOf())
    private var categoryModalVisible by mutableStateOf(false)
    private var descriptionModalVisible by mutableStateOf(false)
    private var deleteTransactionModalVisible by mutableStateOf(false)
    private var transactionTypeModalVisible by mutableStateOf(false)
    private var amountModalVisible by mutableStateOf(false)
    private var recurringRuleModalData by mutableStateOf<RecurringRuleModalData?>(null)
    private var categoryModalData by mutableStateOf<CategoryModalData?>(null)
    private var accountModalData by mutableStateOf<AccountModalData?>(null)

    private var loadedRule: PlannedPaymentRule? = null
    private var editMode = false
    private var title: String? = null
    private val _uiEvents = MutableSharedFlow<EditPlannedUiEvent>()
    val uiEvents: SharedFlow<EditPlannedUiEvent> = _uiEvents.asSharedFlow()

    @Composable
    override fun uiState(): EditPlannedScreenState {
        return EditPlannedScreenState(
            currency = getCurrency(),
            categories = getCategories(),
            accounts = getAccounts(),
            transactionType = getTransactionType(),
            startDate = getStartDate(),
            intervalN = getIntervalN(),
            oneTime = getOneTime(),
            account = getAccount(),
            category = getCategory(),
            amount = getAmount(),
            initialTitle = getInitialTitle(),
            description = getDescription(),
            intervalType = getIntervalType(),
            categoryModalVisible = getCategoryModalVisibility(),
            categoryModalData = getCategoryModalData(),
            accountModalData = getAccountModalData(),
            deleteTransactionModalVisible = getDeleteTransactionModalVisibility(),
            descriptionModalVisible = getDescriptionModalVisibility(),
            amountModalVisible = getAmountModalVisibility(),
            transactionTypeModalVisible = getTransactionTypeModalVisibility(),
            recurringRuleModalData = getRecurringRuleModalData()
        )
    }

    @Composable
    private fun getCurrency(): String {
        return currency
    }

    @Composable
    private fun getCategories(): ImmutableList<Category> {
        return categories
    }

    @Composable
    private fun getAccounts(): ImmutableList<LegacyAccount> {
        return accounts
    }

    @Composable
    private fun getTransactionType(): TransactionType {
        return transactionType
    }

    @Composable
    private fun getStartDate(): LocalDateTime? {
        return startDate
    }

    @Composable
    private fun getIntervalN(): Int? {
        return intervalN
    }

    @Composable
    private fun getIntervalType(): IntervalType? {
        return intervalType
    }

    @Composable
    private fun getOneTime(): Boolean {
        return oneTime
    }

    @Composable
    private fun getInitialTitle(): String? {
        return initialTitle
    }

    @Composable
    private fun getDescription(): String? {
        return description
    }

    @Composable
    private fun getAccount(): LegacyAccount? {
        val selectedAccountId = accountId ?: return null
        return accounts.firstOrNull { it.id == selectedAccountId }
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
    private fun getCategoryModalVisibility(): Boolean {
        return categoryModalVisible
    }

    @Composable
    private fun getDescriptionModalVisibility(): Boolean {
        return descriptionModalVisible
    }

    @Composable
    private fun getDeleteTransactionModalVisibility(): Boolean {
        return deleteTransactionModalVisible
    }

    @Composable
    private fun getTransactionTypeModalVisibility(): Boolean {
        return transactionTypeModalVisible
    }

    @Composable
    private fun getAmountModalVisibility(): Boolean {
        return amountModalVisible
    }

    @Composable
    private fun getCategoryModalData(): CategoryModalData? {
        return categoryModalData
    }

    @Composable
    private fun getAccountModalData(): AccountModalData? {
        return accountModalData
    }

    @Composable
    private fun getRecurringRuleModalData(): RecurringRuleModalData? {
        return recurringRuleModalData
    }

    override fun onEvent(event: EditPlannedScreenEvent) {
        when (event) {
            is EditPlannedScreenEvent.OnSave -> save()
            is EditPlannedScreenEvent.OnDelete -> delete()
            is EditPlannedScreenEvent.OnSetTransactionType ->
                updateTransactionType(event.newTransactionType)

            is EditPlannedScreenEvent.OnDescriptionChanged ->
                updateDescription(event.newDescription)

            is EditPlannedScreenEvent.OnCreateAccount -> createAccount(event.data)
            is EditPlannedScreenEvent.OnCreateCategory -> createCategory(event.data)
            is EditPlannedScreenEvent.OnAccountChanged -> updateAccount(event.accountId)
            is EditPlannedScreenEvent.OnAmountChanged -> updateAmount(event.newAmount)
            is EditPlannedScreenEvent.OnTitleChanged -> updateTitle(event.newTitle)
            is EditPlannedScreenEvent.OnRuleChanged ->
                updateRule(event.startDate, event.oneTime, event.intervalN, event.intervalType)

            is EditPlannedScreenEvent.OnCategoryChanged -> updateCategory(event.categoryId)
            is EditPlannedScreenEvent.OnEditCategory -> editCategory(event.updatedCategory)
            is EditPlannedScreenEvent.OnCategoryModalVisible ->
                categoryModalVisible = event.visible

            is EditPlannedScreenEvent.OnCategoryModalDataChanged ->
                categoryModalData = event.categoryModalData

            is EditPlannedScreenEvent.OnAccountModalDataChanged ->
                accountModalData = event.accountModalData

            is EditPlannedScreenEvent.OnDescriptionModalVisible ->
                descriptionModalVisible = event.visible

            is EditPlannedScreenEvent.OnTransactionTypeModalVisible ->
                transactionTypeModalVisible = event.visible

            is EditPlannedScreenEvent.OnAmountModalVisible ->
                amountModalVisible = event.visible

            is EditPlannedScreenEvent.OnDeleteTransactionModalVisible ->
                deleteTransactionModalVisible = event.visible

            is EditPlannedScreenEvent.OnRecurringRuleModalDataChanged ->
                recurringRuleModalData = event.recurringRuleModalData
        }
    }

    fun start(
        plannedPaymentRuleId: UUID?,
        type: TransactionType,
        amount: Double?,
        accountId: UUID?,
        categoryId: UUID?,
        title: String?,
        description: String?,
    ) {
        viewModelScope.launch {
            transactionType = type
            editMode = plannedPaymentRuleId != null

            val accounts = getLegacyAccountsUseCase()
            if (accounts.isEmpty()) {
                _uiEvents.emit(EditPlannedUiEvent.CloseScreen)
                return@launch
            }
            this@EditPlannedViewModel.accounts = accounts
            categories = getCategoriesUseCase().toImmutableList()

            reset()

            loadedRule = plannedPaymentRuleId?.let {
                getPlannedPaymentRuleUseCase(it) ?: error("planned payment rule not found")
            } ?: PlannedPaymentRule(
                startDate = null,
                intervalN = null,
                intervalType = null,
                oneTime = false,
                type = type,
                amount = amount ?: 0.0,
                accountId = accountId ?: accounts.first().id,
                categoryId = categoryId,
                title = title,
                description = description
            )

            display(loadedRule!!)
        }
    }

    private suspend fun display(rule: PlannedPaymentRule) {
        this.title = rule.title

        transactionType = rule.type
        startDate = rule.startDate?.toLocalDateTimeInSystemZone()
        intervalN = rule.intervalN
        oneTime = rule.oneTime
        intervalType = rule.intervalType
        initialTitle = rule.title
        description = rule.description
        if (accounts.none { it.id == rule.accountId }) error("account not found")
        accountId = rule.accountId
        category = rule.categoryId?.let {
            getCategoryUseCase(CategoryId(it))
        }
        amount = rule.amount

        updateCurrency(accountId = rule.accountId)
    }

    private suspend fun updateCurrency(accountId: UUID?) {
        currency = accounts.firstOrNull { it.id == accountId }?.currency ?: baseCurrency()
    }

    private suspend fun baseCurrency(): String = getBaseCurrencyCode()

    private fun updateRule(
        startDate: LocalDateTime,
        oneTime: Boolean,
        intervalN: Int?,
        intervalType: IntervalType?
    ) {
        loadedRule = loadedRule().copy(
            startDate = startDate.toInstantInSystemZone(),
            intervalN = intervalN,
            intervalType = intervalType,
            oneTime = oneTime
        )
        this@EditPlannedViewModel.startDate = startDate
        this@EditPlannedViewModel.intervalN = intervalN
        this@EditPlannedViewModel.intervalType = intervalType
        this@EditPlannedViewModel.oneTime = oneTime

        saveIfEditMode()
    }

    private fun updateAmount(newAmount: Double) {
        loadedRule = loadedRule().copy(
            amount = newAmount
        )
        this@EditPlannedViewModel.amount = newAmount

        saveIfEditMode()
    }

    private fun updateTitle(newTitle: String?) {
        loadedRule = loadedRule().copy(
            title = newTitle
        )
        this.title = newTitle

        saveIfEditMode()
    }

    private fun updateDescription(newDescription: String?) {
        loadedRule = loadedRule().copy(
            description = newDescription
        )
        this@EditPlannedViewModel.description = newDescription

        saveIfEditMode()
    }

    private fun updateCategory(categoryId: CategoryId?) {
        val newCategory = categories.firstOrNull { it.id == categoryId }
        loadedRule = loadedRule().copy(
            categoryId = categoryId?.value
        )
        this@EditPlannedViewModel.category = newCategory

        saveIfEditMode()
    }

    private fun updateAccount(accountId: UUID) {
        loadedRule = loadedRule().copy(
            accountId = accountId
        )
        this@EditPlannedViewModel.accountId = accountId

        viewModelScope.launch {
            updateCurrency(accountId = accountId)
        }

        saveIfEditMode()
    }

    private fun updateTransactionType(newTransactionType: TransactionType) {
        loadedRule = loadedRule().copy(
            type = newTransactionType
        )
        this@EditPlannedViewModel.transactionType = newTransactionType

        saveIfEditMode()
    }

    private fun saveIfEditMode() {
        if (editMode) {
            save(false)
        }
    }

    private fun save(closeScreen: Boolean = true) {
        if (!validate()) {
            return
        }

        viewModelScope.launch {
            try {
                loadedRule = loadedRule().copy(
                    type = transactionType ?: error("no transaction type"),
                    startDate = startDate?.toInstantInSystemZone()
                        ?: error("no startDate"),
                    intervalN = intervalN ?: error("no intervalN"),
                    intervalType = intervalType ?: error("no intervalType"),
                    categoryId = category?.id?.value,
                    accountId = accountId ?: error("no accountId"),
                    title = title?.trim(),
                    description = description?.trim(),
                    amount = amount ?: error("no amount"),
                )

                savePlannedPaymentRuleUseCase(loadedRule())

                if (closeScreen) {
                    _uiEvents.emit(EditPlannedUiEvent.CloseScreen)
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun validate(): Boolean {
        if (transactionType == TransactionType.TRANSFER) {
            return false
        }

        if (amount == 0.0) {
            return false
        }

        return if (oneTime) validateOneTime() else validateRecurring()
    }

    private fun validateOneTime(): Boolean {
        return startDate != null
    }

    private fun validateRecurring(): Boolean {
        return startDate != null &&
                intervalN != null &&
                intervalN!! > 0 &&
                intervalType != null
    }

    private fun delete() {
        viewModelScope.launch {
            deleteTransactionModalVisible = false
            loadedRule?.let {
                deletePlannedPaymentRuleUseCase(it.id)
            }
            _uiEvents.emit(EditPlannedUiEvent.CloseScreen)
        }
    }

    private fun createCategory(data: CreateCategoryData) {
        viewModelScope.launch {
            createCategoryUseCase(data)?.let {
                categories = getCategoriesUseCase().toImmutableList()

                updateCategory(it.id)
            }
        }
    }

    private fun editCategory(updatedCategory: Category) {
        viewModelScope.launch {
            if (updateCategoryUseCase(updatedCategory)) {
                categories = getCategoriesUseCase().toImmutableList()
            }
        }
    }

    private fun createAccount(data: CreateAccountData) {
        viewModelScope.launch {
            createAccountWithBalanceUseCase(data)
            accounts = getLegacyAccountsUseCase()
        }
    }

    private fun reset() {
        loadedRule = null

        initialTitle = null
        description = null
        accountId = null
        category = null
    }

    private fun loadedRule() = loadedRule ?: error("Loaded transaction is null")

    private fun Instant.toLocalDateTimeInSystemZone() =
        atZone(ZoneId.systemDefault()).toLocalDateTime()

    private fun LocalDateTime.toInstantInSystemZone() =
        atZone(ZoneId.systemDefault()).toInstant()
}
