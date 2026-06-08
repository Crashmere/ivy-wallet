package com.ivy.transactions

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.data.model.Category
import com.ivy.data.model.Expense
import com.ivy.data.model.Income
import com.ivy.data.model.Theme
import com.ivy.data.model.Tag
import com.ivy.data.model.Transaction
import com.ivy.data.model.TransactionHistoryDateDivider
import com.ivy.data.model.TransactionHistoryItem
import com.ivy.data.model.TransactionHistoryTransaction
import com.ivy.data.model.TransactionType
import com.ivy.data.model.Transfer
import com.ivy.data.model.getFromAccount
import com.ivy.data.model.getFromValue
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.IconAsset
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.ui.platform.LocalDatePicker
import com.ivy.ui.theme.LocalThemeState
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.ui.compose.thenIf
import com.ivy.ui.transaction.TransactionListData
import com.ivy.ui.transaction.TransactionListAccount
import com.ivy.ui.transaction.TransactionListCategory
import com.ivy.ui.transaction.TransactionListDueSection
import com.ivy.ui.transaction.TransactionListHistoryDateDivider
import com.ivy.ui.transaction.TransactionListHistoryItem
import com.ivy.ui.transaction.TransactionListHistoryTransaction
import com.ivy.ui.transaction.TransactionListTag
import com.ivy.ui.transaction.TransactionListTransaction
import com.ivy.ui.transaction.TransactionListTransactionType
import com.ivy.ui.period.Month
import com.ivy.ui.period.TimePeriod
import com.ivy.ui.period.displayLong
import com.ivy.ui.period.LocalPeriodState
import com.ivy.ui.summary.IncomeExpensesCards
import com.ivy.ui.transaction.transactions
import com.ivy.ui.money.balancePrefix
import com.ivy.ui.compose.clickableNoIndication
import com.ivy.ui.compose.horizontalSwipeListener
import com.ivy.ui.compose.rememberInteractionSource
import com.ivy.ui.compose.rememberSwipeListenerState
import com.ivy.ui.platform.setStatusBarDarkTextCompat
import com.ivy.ui.navigation.EditTransactionScreen
import com.ivy.ui.navigation.PieChartStatisticScreen
import com.ivy.ui.navigation.TransactionRouteType
import com.ivy.ui.navigation.TransactionsScreen
import com.ivy.ui.navigation.navigation
import com.ivy.ui.navigation.onScreenStart
import com.ivy.ui.navigation.screenScopedViewModel
import com.ivy.ui.R
import com.ivy.ui.rememberScrollPositionListState
import com.ivy.ui.money.BalanceRow
import com.ivy.ui.compose.GradientIconButton
import com.ivy.ui.compose.OutlinedPillButton
import com.ivy.ui.icon.ItemIconMDefaultIcon
import com.ivy.ui.theme.colors.Gradient
import com.ivy.ui.theme.colors.IvyFixedColors.White
import com.ivy.ui.theme.colors.dynamicContrast
import com.ivy.ui.theme.colors.findContrastTextColor
import com.ivy.ui.theme.colors.isDarkColor
import com.ivy.ui.modal.ChoosePeriodModal
import com.ivy.ui.modal.AccountModalAccount
import com.ivy.ui.modal.DeleteModal
import com.ivy.legacy.ui.modal.edit.AccountModal
import com.ivy.legacy.ui.modal.edit.CategoryModal
import com.ivy.legacy.ui.modal.edit.CategoryModalCategory
import com.ivy.legacy.ui.modal.edit.CategoryModalSaveData
import com.ivy.ui.theme.colors.toComposeColor
import com.ivy.ui.period.PeriodSelector
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import java.util.UUID

@Composable
fun BoxWithConstraintsScope.TransactionsScreen(screen: TransactionsScreen) {
    val viewModel: TransactionsViewModel = screenScopedViewModel()

    val themeState = LocalThemeState.current
    val nav = navigation()
    val uiState = viewModel.uiState()

    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                TransactionsUiEvent.CloseScreen -> nav.back()
            }
        }
    }

    val view = LocalView.current
    BackHandler(enabled = !nav.backStackEmpty()) {
        setStatusBarDarkTextCompat(
            view = view,
            darkText = themeState.theme == Theme.LIGHT
        )
        nav.back()
    }

    onScreenStart {
        viewModel.start(screen.toQuery())
    }

    UI(
        screen = screen,
        period = uiState.period,
        baseCurrency = uiState.baseCurrency,
        currency = uiState.currency,

        categories = uiState.categories,
        accounts = uiState.accounts,

        account = uiState.account,
        category = uiState.category,

        balance = uiState.balance,
        balanceBaseCurrency = uiState.balanceBaseCurrency,
        income = uiState.income,
        expenses = uiState.expenses,
        incomeTransactionCount = uiState.incomeTransactionCount,
        expenseTransactionCount = uiState.expenseTransactionCount,

        initWithTransactions = uiState.initWithTransactions,
        treatTransfersAsIncomeExpense = uiState.treatTransfersAsIncomeExpense,

        history = uiState.history,
        shouldShowAccountSpecificColorInTransactions = uiState.showAccountColorsInTransactions,

        upcoming = uiState.upcoming,
        setUpcomingExpanded = {
            viewModel.onEvent(TransactionsEvent.SetUpcomingExpanded(it))
        },

        overdue = uiState.overdue,
        setOverdueExpanded = {
            viewModel.onEvent(TransactionsEvent.SetOverdueExpanded(it))
        },

        onSetPeriod = {
            viewModel.onEvent(
                TransactionsEvent.SetPeriod(
                    period = it
                )
            )
        },
        onNextMonth = {
            viewModel.onEvent(TransactionsEvent.NextMonth)
        },
        onPreviousMonth = {
            viewModel.onEvent(TransactionsEvent.PreviousMonth)
        },
        onDelete = {
            viewModel.onEvent(TransactionsEvent.Delete)
        },
        onEditCategory = {
            viewModel.onEvent(TransactionsEvent.EditCategory(it))
        },
        onEditAccount = { accountId, newBalance ->
            viewModel.onEvent(TransactionsEvent.EditAccount(accountId, newBalance))
        },
        onPayOrGet = { transactionId ->
            viewModel.onEvent(TransactionsEvent.PayOrGet(transactionId))
        },
        onSkipTransaction = { transactionId ->
            viewModel.onEvent(TransactionsEvent.SkipTransaction(transactionId))
        },
        onSkipAllTransactions = { transactionIds ->
            viewModel.onEvent(TransactionsEvent.SkipTransactions(transactionIds))
        },
        onTransactionClick = { transactionId, transactionType ->
            nav.navigateTo(
                EditTransactionScreen(
                    initialTransactionId = transactionId,
                    type = transactionType.toRouteType()
                )
            )
        },
        onAccountClick = {
            nav.navigateTo(
                TransactionsScreen(
                    accountId = it,
                    categoryId = null
                )
            )
        },
        onCategoryClick = {
            nav.navigateTo(
                TransactionsScreen(
                    accountId = null,
                    categoryId = it
                )
            )
        },
        onClose = nav::back,
        onOpenPieChart = { transactionType, accountId, treatTransfersAsIncomeExpense ->
            nav.navigateTo(
                PieChartStatisticScreen(
                    type = transactionType.toRouteType(),
                    accountIdFilterList = persistentListOf(accountId),
                    filterExcluded = false,
                    treatTransfersAsIncomeExpense = treatTransfersAsIncomeExpense
                )
            )
        },
        onAddTransaction = { transactionType, accountId, categoryId ->
            nav.navigateTo(
                EditTransactionScreen(
                    initialTransactionId = null,
                    type = transactionType.toRouteType(),
                    accountId = accountId,
                    categoryId = categoryId
                )
            )
        },
        updateAccountNameConfirmation = {
            viewModel.onEvent(TransactionsEvent.UpdateAccountDeletionState(it))
        },
        enableDeletionButton = uiState.enableDeletionButton,
        skipAllModalVisible = uiState.skipAllModalVisible,
        onSkipAllModalVisible = {
            viewModel.onEvent(TransactionsEvent.SetSkipAllModalVisible(it))
        },
        deleteModal1Visible = uiState.deleteModal1Visible,
        onDeleteModal1Visible = {
            viewModel.onEvent(TransactionsEvent.OnDeleteModal1Visible(it))
        }
    )
}

@Suppress("LongMethod", "LongParameterList")
@Composable
private fun BoxWithConstraintsScope.UI(
    screen: TransactionsScreen,
    period: TimePeriod,
    baseCurrency: String,
    currency: String,
    skipAllModalVisible: Boolean,
    onSkipAllModalVisible: (Boolean) -> Unit,

    account: TransactionsAccount?,
    category: Category?,

    updateAccountNameConfirmation: (String) -> Unit,
    enableDeletionButton: Boolean,

    categories: ImmutableList<Category>,
    accounts: ImmutableList<TransactionsListAccount>,

    balance: Double,
    balanceBaseCurrency: Double?,
    income: Double,
    expenses: Double,
    incomeTransactionCount: Int,
    expenseTransactionCount: Int,

    history: ImmutableList<TransactionHistoryItem>,
    shouldShowAccountSpecificColorInTransactions: Boolean,

    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSetPeriod: (TimePeriod) -> Unit,
    onEditAccount: (UUID, Double) -> Unit,
    onEditCategory: (Category) -> Unit,
    onDelete: () -> Unit,
    deleteModal1Visible: Boolean,
    onDeleteModal1Visible: (Boolean) -> Unit,

    initWithTransactions: Boolean = false,
    treatTransfersAsIncomeExpense: Boolean = false,
    setUpcomingExpanded: (Boolean) -> Unit = {},
    upcoming: TransactionsDueSection = TransactionsDueSection(
        transactions = persistentListOf(),
        expanded = true,
        income = 0.0,
        expenses = 0.0,
    ),

    setOverdueExpanded: (Boolean) -> Unit = {},
    overdue: TransactionsDueSection = TransactionsDueSection(
        transactions = persistentListOf(),
        expanded = true,
        income = 0.0,
        expenses = 0.0,
    ),

    onPayOrGet: (UUID) -> Unit = {},
    onSkipTransaction: (UUID) -> Unit = {},
    onSkipAllTransactions: (List<UUID>) -> Unit = {},
    onTransactionClick: (UUID, TransactionType) -> Unit,
    onAccountClick: (UUID) -> Unit,
    onCategoryClick: (UUID) -> Unit,
    onClose: () -> Unit,
    onOpenPieChart: (TransactionType, UUID, Boolean) -> Unit,
    onAddTransaction: (TransactionType, UUID?, UUID?) -> Unit,
) {
    val periodState = LocalPeriodState.current
    val datePicker = LocalDatePicker.current
    val screenHeight = maxHeight
    val itemColor = (account?.color ?: category?.color?.value)?.toComposeColor()
        ?: LegacyTheme.colors.gray

    var categoryModalVisible by remember { mutableStateOf(false) }
    var categoryModalCategory: Category? by remember { mutableStateOf(null) }
    var categoryModalAutoFocus by remember { mutableStateOf(true) }
    var accountModalVisible by remember { mutableStateOf(false) }
    var accountModalAccount: AccountModalAccount? by remember { mutableStateOf(null) }
    var accountModalBaseCurrency by remember { mutableStateOf("") }
    var accountModalBalance by remember { mutableStateOf(0.0) }
    var accountModalAdjustBalanceMode by remember { mutableStateOf(false) }
    var accountModalAutoFocus by remember { mutableStateOf(true) }
    var choosePeriodModal: TimePeriod? by remember { mutableStateOf(null) }
    var skipAllTransactionIds by remember { mutableStateOf<List<UUID>>(emptyList()) }
    fun showAccountModal(
        modalAccount: AccountModalAccount?,
        modalBaseCurrency: String,
        modalBalance: Double,
        adjustBalanceMode: Boolean,
        autoFocusKeyboard: Boolean,
    ) {
        accountModalAccount = modalAccount
        accountModalBaseCurrency = modalBaseCurrency
        accountModalBalance = modalBalance
        accountModalAdjustBalanceMode = adjustBalanceMode
        accountModalAutoFocus = autoFocusKeyboard
        accountModalVisible = true
    }

    val swipeListenerState = rememberSwipeListenerState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(itemColor)
            .thenIf(!initWithTransactions) {
                horizontalSwipeListener(
                    sensitivity = 150,
                    state = swipeListenerState,
                    onSwipeLeft = {
                        onNextMonth()
                    },
                    onSwipeRight = {
                        onPreviousMonth()
                    }
                )
            }
    ) {
        val listState = rememberScrollPositionListState(
            key = "item_stats_lazy_column"
        )

        val noTransactionsTitle = stringResource(R.string.no_transactions)
        val noTransactionsText = stringResource(
            R.string.no_transactions_for_period,
            period.displayLong(periodState.startDayOfMonth)
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 16.dp)
                .clip(LegacyTheme.shapes.r1Top)
                .background(LegacyTheme.colors.pure)
                .testTag("item_stats_lazy_column"),
            state = listState,
        ) {
            item {
                Header(
                    screen = screen,
                    income = income,
                    expenses = expenses,
                    incomeTransactionCount = incomeTransactionCount,
                    expenseTransactionCount = expenseTransactionCount,
                    currency = currency,
                    baseCurrency = baseCurrency,
                    itemColor = itemColor,
                    account = account,
                    category = category,
                    balance = balance,
                    balanceBaseCurrency = balanceBaseCurrency,
                    treatTransfersAsIncomeExpense = treatTransfersAsIncomeExpense,

                    onDelete = {
                        onDeleteModal1Visible(true)
                    },
                    onEdit = {
                        when {
                            account != null -> {
                                showAccountModal(
                                    modalAccount = account.toAccountModalAccount(),
                                    modalBaseCurrency = currency,
                                    modalBalance = balance,
                                    adjustBalanceMode = false,
                                    autoFocusKeyboard = false
                                )
                            }

                            category != null -> {
                                categoryModalCategory = category
                                categoryModalAutoFocus = false
                                categoryModalVisible = true
                            }
                        }
                    },

                    onBalanceClick = {
                        when {
                            account != null -> {
                                showAccountModal(
                                    modalAccount = account.toAccountModalAccount(),
                                    modalBaseCurrency = currency,
                                    modalBalance = balance,
                                    adjustBalanceMode = true,
                                    autoFocusKeyboard = false
                                )
                            }
                        }
                    },
                    showCategoryModal = {
                        categoryModalCategory = category
                        categoryModalAutoFocus = false
                        categoryModalVisible = true
                    },
                    showAccountModal = {
                        showAccountModal(
                            modalAccount = account?.toAccountModalAccount(),
                            modalBaseCurrency = currency,
                            modalBalance = balance,
                            adjustBalanceMode = false,
                            autoFocusKeyboard = false
                        )
                    },
                    onClose = onClose,
                    onOpenPieChart = onOpenPieChart,
                    onAddTransaction = onAddTransaction
                )
            }

            choosePeriodModal(
                period = period,
                startDateOfMonth = periodState.startDayOfMonth,
                itemColor = itemColor,
                initWithTransactions = initWithTransactions,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onChoosePeriodModal = { choosePeriodModal = it }
            )

            transactions(
                baseData = TransactionListData(
                    baseCurrency,
                    accounts
                        .map { it.toTransactionListAccount() },
                    categories.map { it.toTransactionListCategory() }
                ),
                upcoming = upcoming.toTransactionListDueSection(),
                setUpcomingExpanded = setUpcomingExpanded,

                overdue = overdue.toTransactionListDueSection(),
                setOverdueExpanded = setOverdueExpanded,

                history = history.map { it.toTransactionListHistoryItem() },
                lastItemSpacer = screenHeight * 0.7f,

                onPayOrGet = onPayOrGet,
                onTransactionClick = { transactionId, transactionType ->
                    onTransactionClick(transactionId, transactionType.toTransactionType())
                },
                onAccountClick = onAccountClick,
                onCategoryClick = onCategoryClick,
                onSkipTransaction = onSkipTransaction,
                onSkipAllTransactions = { transactionIds ->
                    skipAllTransactionIds = transactionIds
                    onSkipAllModalVisible(true)
                },
                emptyStateTitle = noTransactionsTitle,
                emptyStateText = noTransactionsText,
                shouldShowAccountSpecificColorInTransactions = shouldShowAccountSpecificColorInTransactions
            )
        }
    }

    DeleteModals(
        account = account,
        category = category,
        updateAccountNameConfirmation = updateAccountNameConfirmation,
        enableDeletionButton = enableDeletionButton,
        onDelete = onDelete,
        skipAllModalVisible = skipAllModalVisible,
        onSkipAllModalVisible = {
            onSkipAllModalVisible(it)
        },
        onSkipAllTransactions = onSkipAllTransactions,
        skipAllTransactionIds = skipAllTransactionIds,
        deleteModal1Visible = deleteModal1Visible,
        setDeleteModal1Visible = onDeleteModal1Visible
    )

    CategoryModal(
        visible = categoryModalVisible,
        category = categoryModalCategory?.toCategoryModalCategory(),
        autoFocusKeyboard = categoryModalAutoFocus,
        onCreateCategory = { _ -> },
        onEditCategory = { _, data ->
            val editedCategory = categoryModalCategory?.withModalSaveData(data)
            if (editedCategory != null) {
                onEditCategory(editedCategory)
            }
        },
        dismiss = {
            categoryModalVisible = false
        }
    )

    AccountModal(
        visible = accountModalVisible,
        account = accountModalAccount,
        baseCurrency = accountModalBaseCurrency,
        balance = accountModalBalance,
        adjustBalanceMode = accountModalAdjustBalanceMode,
        autoFocusKeyboard = accountModalAutoFocus,
        onCreateAccount = { _ -> },
        onEditAccount = { accountId, data ->
            onEditAccount(accountId, data.balance)
        },
        dismiss = {
            accountModalVisible = false
        }
    )

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
        onSetPeriod(it)
    }
}

private fun TransactionsDueSection.toTransactionListDueSection(): TransactionListDueSection {
    return TransactionListDueSection(
        transactions = transactions.map { it.toTransactionListTransaction() },
        income = income,
        expenses = expenses,
        expanded = expanded
    )
}

private fun TransactionHistoryItem.toTransactionListHistoryItem(): TransactionListHistoryItem {
    return when (this) {
        is TransactionHistoryTransaction -> TransactionListHistoryTransaction(
            transaction = transaction.toTransactionListTransaction(),
            tags = tags.map { it.toTransactionListTag() },
        )

        is TransactionHistoryDateDivider -> TransactionListHistoryDateDivider(
            date = date,
            income = income,
            expenses = expenses,
        )

        else -> error("Unsupported transaction history item: ${this::class.simpleName}")
    }
}

private fun Tag.toTransactionListTag() = TransactionListTag(
    id = id.value,
    name = name.value,
)

private fun TransactionsListAccount.toTransactionListAccount() = TransactionListAccount(
    id = id,
    name = name,
    color = color,
    icon = icon,
    currency = currency,
)

private fun Category.toTransactionListCategory() = TransactionListCategory(
    id = id.value,
    name = name.value,
    color = color.value,
    icon = icon?.id,
)

private fun TransactionListTransactionType.toTransactionType(): TransactionType {
    return TransactionType.valueOf(name)
}

private fun Transaction.toTransactionListTransaction(): TransactionListTransaction {
    val amount = getFromValue().amount.value.toBigDecimal()
    return TransactionListTransaction(
        id = id.value,
        accountId = getFromAccount().value,
        type = when (this) {
            is Expense -> TransactionListTransactionType.EXPENSE
            is Income -> TransactionListTransactionType.INCOME
            is Transfer -> TransactionListTransactionType.TRANSFER
        },
        amount = amount,
        toAccountId = if (this is Transfer) toAccount.value else null,
        toAmount = if (this is Transfer) toValue.amount.value.toBigDecimal() else amount,
        title = title?.value,
        description = description?.value,
        dateTime = time.takeIf { settled },
        categoryId = category?.value,
        dueDate = time.takeIf { !settled },
        recurringRuleId = metadata.recurringRuleId,
        paidFor = metadata.paidForDateTime,
    )
}

private fun LazyListScope.choosePeriodModal(
    period: TimePeriod,
    startDateOfMonth: Int,
    itemColor: Color,
    initWithTransactions: Boolean,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onChoosePeriodModal: (TimePeriod?) -> Unit,
) {
    item {
        // Rounded corners top effect
        Box {
            Spacer(
                Modifier
                    .height(32.dp)
                    .fillMaxWidth()
                    .background(itemColor) // itemColor is displayed below the clip
                    .background(LegacyTheme.colors.pure, LegacyTheme.shapes.r1Top)
            )

            PeriodSelector(
                modifier = Modifier.padding(top = 16.dp),
                period = period,
                startDateOfMonth = startDateOfMonth,
                onPreviousMonth = { if (!initWithTransactions) onPreviousMonth() },
                onNextMonth = { if (!initWithTransactions) onNextMonth() },
                onShowChoosePeriodModal = {
                    if (!initWithTransactions) {
                        onChoosePeriodModal(period)
                    }
                }
            )
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.DeleteModals(
    deleteModal1Visible: Boolean,
    setDeleteModal1Visible: (Boolean) -> Unit,
    account: TransactionsAccount?,
    category: Category?,
    updateAccountNameConfirmation: (String) -> Unit,
    enableDeletionButton: Boolean,
    onDelete: () -> Unit,
    skipAllModalVisible: Boolean,
    onSkipAllModalVisible: (Boolean) -> Unit,
    onSkipAllTransactions: (List<UUID>) -> Unit,
    skipAllTransactionIds: List<UUID> = emptyList(),
) {
    var deleteModal3Visible by remember { mutableStateOf(false) }

    DeleteModal(
        visible = deleteModal1Visible,
        title = stringResource(R.string.confirm_deletion),
        description = if (account != null) {
            stringResource(R.string.account_confirm_deletion_description)
        } else {
            stringResource(R.string.category_confirm_deletion_description)
        },
        dismiss = {
            setDeleteModal1Visible(false)
        }
    ) {
        deleteModal3Visible = true
    }

    TransactionsDeleteConfirmationModal(
        visible = deleteModal3Visible,
        title = stringResource(id = R.string.confirm_deletion),
        description = if (account != null) {
            stringResource(
                id = R.string.account_confirm_deletion_type_account_name,
                account.name
            )
        } else {
            stringResource(R.string.please_type_category_name, category?.name?.value ?: "")
        },
        hint = if (account != null) stringResource(id = R.string.account_name) else "Category name",
        onAccountNameChange = updateAccountNameConfirmation,
        enableDeletionButton = enableDeletionButton,
        dismiss = {
            updateAccountNameConfirmation("")
            deleteModal3Visible = false
            setDeleteModal1Visible(false)
        }
    ) {
        onDelete()
        updateAccountNameConfirmation("")
        setDeleteModal1Visible(false)
    }

    DeleteModal(
        visible = skipAllModalVisible,
        title = stringResource(R.string.confirm_skip_all),
        description = stringResource(R.string.confirm_skip_all_description),
        dismiss = {
            onSkipAllModalVisible(false)
        }
    ) {
        onSkipAllTransactions(skipAllTransactionIds)
        onSkipAllModalVisible(false)
    }
}

@Suppress("LongParameterList")
@Composable
private fun Header(
    screen: TransactionsScreen,
    currency: String,
    baseCurrency: String,
    itemColor: Color,
    account: TransactionsAccount?,
    category: Category?,
    balance: Double,
    balanceBaseCurrency: Double?,
    income: Double,
    expenses: Double,
    incomeTransactionCount: Int,
    expenseTransactionCount: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit,

    onBalanceClick: () -> Unit,
    showCategoryModal: () -> Unit,
    showAccountModal: () -> Unit,
    onClose: () -> Unit,
    onOpenPieChart: (TransactionType, UUID, Boolean) -> Unit,
    onAddTransaction: (TransactionType, UUID?, UUID?) -> Unit,
    treatTransfersAsIncomeExpense: Boolean = false,
) {
    val contrastColor = findContrastTextColor(itemColor)

    val darkColor = isDarkColor(itemColor)
    setStatusBarDarkTextCompat(darkText = !darkColor)

    Column(
        modifier = Modifier.background(itemColor)
    ) {
        Spacer(Modifier.height(20.dp))

        val hideEditAndDeleteButtonForAccountTransfer =
            !screen.containsTransferTransactions

        TransactionsStatisticToolbar(
            contrastColor = contrastColor,
            onClose = onClose,
            onEdit = onEdit,
            onDelete = onDelete,
            showEditButton = hideEditAndDeleteButtonForAccountTransfer,
            showDeleteButton = hideEditAndDeleteButtonForAccountTransfer,
        )

        Spacer(Modifier.height(24.dp))

        Item(
            contrastColor = contrastColor,
            account = account,
            category = category,
            showAccountModal = showAccountModal,
            showCategoryModal = showCategoryModal
        )

        BalanceRow(
            modifier = Modifier
                .padding(start = 32.dp)
                .testTag("balance")
                .clickableNoIndication(rememberInteractionSource()) {
                    onBalanceClick()
                },
            textColor = contrastColor,
            currency = currency,
            balance = balance,
            balanceAmountPrefix = if (category != null) {
                balancePrefix(
                    income = income,
                    expenses = expenses
                )
            } else {
                null
            }
        )

        if (currency != baseCurrency && balanceBaseCurrency != null) {
            BalanceRow(
                modifier = Modifier
                    .padding(start = 32.dp)
                    .clickableNoIndication(rememberInteractionSource()) {
                        onBalanceClick()
                    },
                textColor = itemColor.dynamicContrast(),
                currency = baseCurrency,
                balance = balanceBaseCurrency,
                spacerCurrency = 12.dp,
                currencyFontSize = 24.sp,
                balanceFontSize = 26.sp,
                balanceAmountPrefix = if (category != null) {
                    balancePrefix(
                        income = income,
                        expenses = expenses
                    )
                } else {
                    null
                }
            )
        }

        Spacer(Modifier.height(20.dp))

        IncomeExpensesCards(
            currency = currency,
            income = income,
            expenses = expenses,
            incomeTransactionCount = incomeTransactionCount,
            expenseTransactionCount = expenseTransactionCount,

            hasAddButtons = true,

            itemColor = itemColor,
            incomeHeaderCardClicked = {
                if (account != null) {
                    onOpenPieChart(
                        TransactionType.INCOME,
                        account.id,
                        treatTransfersAsIncomeExpense
                    )
                }
            },
            expenseHeaderCardClicked = {
                if (account != null) {
                    onOpenPieChart(
                        TransactionType.EXPENSE,
                        account.id,
                        treatTransfersAsIncomeExpense
                    )
                }
            },
            onAddIncome = {
                onAddTransaction(
                    TransactionType.INCOME,
                    account?.id,
                    category?.id?.value
                )
            },
            onAddExpense = {
                onAddTransaction(
                    TransactionType.EXPENSE,
                    account?.id,
                    category?.id?.value
                )
            }
        )

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun TransactionsStatisticToolbar(
    contrastColor: Color,
    onClose: () -> Unit,
    onEdit: () -> Unit,
    showEditButton: Boolean = true,
    showDeleteButton: Boolean = true,
    onDelete: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(24.dp))

        StatisticToolbarCloseButton(
            contrastColor = contrastColor,
            onClose = onClose
        )

        Spacer(Modifier.weight(1f))

        if (showEditButton) {
            OutlinedPillButton(
                iconStart = R.drawable.ic_edit,
                text = stringResource(R.string.edit),
                shape = LegacyTheme.shapes.rFull,
                solidBackground = false,
                backgroundColor = LegacyTheme.colors.pure,
                borderColor = contrastColor,
                iconTint = contrastColor,
                textStyle = LegacyTheme.typo.b2.copy(
                    fontWeight = FontWeight.Bold,
                    color = contrastColor,
                    textAlign = TextAlign.Start,
                ),
            ) {
                onEdit()
            }
        }

        Spacer(Modifier.width(16.dp))

        if (showDeleteButton) {
            StatisticToolbarDeleteButton {
                onDelete()
            }
        }

        Spacer(Modifier.width(24.dp))
    }
}

@Composable
private fun StatisticToolbarCloseButton(
    contrastColor: Color,
    onClose: () -> Unit
) {
    Icon(
        modifier = Modifier
            .testTag("toolbar_close")
            .clip(CircleShape)
            .background(Color.Transparent, CircleShape)
            .border(2.dp, contrastColor, CircleShape)
            .clickable(onClick = onClose)
            .padding(6.dp),
        painter = painterResource(id = R.drawable.ic_dismiss),
        contentDescription = "close",
        tint = contrastColor,
    )
}

@Composable
private fun StatisticToolbarDeleteButton(
    onDelete: () -> Unit
) {
    GradientIconButton(
        modifier = Modifier
            .size(48.dp)
            .testTag("delete_button"),
        backgroundPadding = 6.dp,
        icon = R.drawable.ic_delete,
        backgroundGradient = Gradient.solid(LegacyTheme.colors.red),
        enabled = true,
        tint = White,
        onClick = onDelete
    )
}

@Composable
private fun Item(
    contrastColor: Color,
    account: TransactionsAccount?,
    category: Category?,

    showCategoryModal: () -> Unit,
    showAccountModal: () -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(start = 22.dp)
            .clickableNoIndication(rememberInteractionSource()) {
                when {
                    account != null -> {
                        showAccountModal()
                    }

                    category != null -> {
                        showCategoryModal()
                    }
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        when {
            account != null -> {
                ItemIconMDefaultIcon(
                    iconName = account.icon,
                    defaultIcon = R.drawable.ic_custom_account_m,
                    tint = contrastColor
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = account.name,
                    style = LegacyTheme.typo.b1.copy(
                        color = contrastColor,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Start
                    )
                )

                if (!account.includeInBalance) {
                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = stringResource(R.string.excluded),
                        style = LegacyTheme.typo.c.copy(
                            color = account.color.toComposeColor().dynamicContrast(),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Start
                        )
                    )
                }
            }

            category != null -> {
                ItemIconMDefaultIcon(
                    iconName = category.icon?.id,
                    defaultIcon = R.drawable.ic_custom_category_m,
                    tint = contrastColor
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = category.name.value,
                    style = LegacyTheme.typo.b1.copy(
                        color = contrastColor,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Start
                    )
                )
            }

            else -> {
                // Unspecified
                ItemIconMDefaultIcon(
                    iconName = null,
                    defaultIcon = R.drawable.ic_custom_category_m,
                    tint = contrastColor
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = stringResource(R.string.unspecified),
                    style = LegacyTheme.typo.b1.copy(
                        color = contrastColor,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Start
                    )
                )
            }
        }
    }
}

private fun TransactionType.toRouteType(): TransactionRouteType {
    return TransactionRouteType.valueOf(name)
}

private fun TransactionsScreen.toQuery() = TransactionsQuery(
    accountId = accountId,
    categoryId = categoryId,
    unspecifiedCategory = unspecifiedCategory,
    accountIdFilterList = accountIdFilterList,
    transactionIds = transactionIds,
)

private fun Category.toCategoryModalCategory() = CategoryModalCategory(
    id = id.value,
    name = name.value,
    color = color.value,
    icon = icon?.id,
)

private fun Category.withModalSaveData(data: CategoryModalSaveData) = copy(
    name = NotBlankTrimmedString.unsafe(data.name),
    color = ColorInt(data.color),
    icon = data.icon?.let { IconAsset.unsafe(it) },
)
