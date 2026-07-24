package com.ivy.transaction

import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.CopyAll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.data.model.TransactionType
import com.ivy.data.model.Category
import com.ivy.data.model.Tag
import com.ivy.data.model.TagId
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.IconAsset
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.ui.platform.LocalDatePicker
import com.ivy.ui.platform.hideKeyboard
import com.ivy.ui.tags.ShowTagModal
import com.ivy.ui.tags.TagModalTag
import com.ivy.ui.tags.AddTagButton
import com.ivy.ui.navigation.onScreenStart
import com.ivy.ui.navigation.EditPlannedScreen
import com.ivy.ui.navigation.EditTransactionScreen
import com.ivy.ui.navigation.MainScreen
import com.ivy.ui.navigation.TransactionRouteType
import com.ivy.ui.navigation.navigation
import com.ivy.ui.navigation.screenScopedViewModel
import com.ivy.ui.R
import com.ivy.data.model.currency.IvyCurrency
import com.ivy.data.model.CreateAccountData
import com.ivy.data.model.CreateCategoryData
import com.ivy.ui.modal.DeleteModal
import com.ivy.ui.modal.ProgressModal
import com.ivy.ui.modal.AccountModal
import com.ivy.ui.modal.AccountModalSaveData
import com.ivy.ui.modal.AmountModal
import com.ivy.ui.modal.CategoryModal
import com.ivy.ui.modal.CategoryModalCategory
import com.ivy.ui.modal.CategoryModalSaveData
import com.ivy.ui.compose.GradientButton
import com.ivy.ui.compose.ResourceIcon
import com.ivy.ui.compose.clickableNoIndication
import com.ivy.ui.compose.rememberInteractionSource
import com.ivy.ui.compose.thenIf
import com.ivy.ui.icon.ItemIconSDefaultIcon
import com.ivy.ui.money.BalanceRow
import com.ivy.ui.theme.colors.Gradient
import com.ivy.ui.theme.colors.IvyGradients
import com.ivy.ui.theme.colors.findContrastTextColor
import com.ivy.ui.theme.colors.toComposeColor
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import kotlin.math.roundToInt

@ExperimentalFoundationApi
@Composable
fun BoxWithConstraintsScope.EditTransactionScreen(screen: EditTransactionScreen) {
    val viewModel: EditTransactionViewModel = screenScopedViewModel()
    val nav = navigation()
    val uiState = viewModel.uiState()

    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                EditTransactionUiEvent.CloseScreen -> {
                    if (nav.backStackEmpty()) {
                        nav.resetBackStack()
                        nav.navigateTo(MainScreen)
                    } else {
                        nav.back()
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.start(
            initialTransactionId = screen.initialTransactionId,
            type = screen.type.toTransactionType(),
            accountId = screen.accountId,
            categoryId = screen.categoryId
        )
    }

    val view = LocalView.current

    UI(
        screen = screen,
        transactionType = uiState.transactionType,
        baseCurrency = uiState.currency,
        initialTitle = uiState.initialTitle,
        titleSuggestions = uiState.titleSuggestions,
        description = uiState.description,
        dateTime = uiState.dateTime,
        category = uiState.category,
        account = uiState.account,
        toAccount = uiState.toAccount,
        dueDate = uiState.dueDate,
        amount = uiState.amount,
        loanData = uiState.displayLoanHelper,
        backgroundProcessing = uiState.backgroundProcessingStarted,
        customExchangeRateState = uiState.customExchangeRateState,

        categories = uiState.categories,
        accounts = uiState.accounts,
        tags = uiState.tags,
        transactionAssociatedTags = uiState.transactionAssociatedTags,
        hasChanges = uiState.hasChanges,
        onSetDate = {
            viewModel.onEvent(EditTransactionViewEvent.OnChangeDate)
        },
        onSetTime = {
            viewModel.onEvent(EditTransactionViewEvent.OnChangeTime)
        },
        onTitleChange = {
            viewModel.onEvent(EditTransactionViewEvent.OnTitleChanged(it))
        },
        onDescriptionChange = {
            viewModel.onEvent(EditTransactionViewEvent.OnDescriptionChanged(it))
        },
        onAmountChange = {
            viewModel.onEvent(EditTransactionViewEvent.OnAmountChanged(it))
        },
        onCategoryChange = {
            viewModel.onEvent(EditTransactionViewEvent.OnCategoryChanged(it?.id))
        },
        onAccountChange = {
            viewModel.onEvent(EditTransactionViewEvent.OnAccountChanged(it.id))
        },
        onToAccountChange = {
            viewModel.onEvent(EditTransactionViewEvent.OnToAccountChanged(it.id))
        },
        onDueDateChange = {
            viewModel.onEvent(EditTransactionViewEvent.OnDueDateChanged(it))
        },
        onSetTransactionType = {
            viewModel.onEvent(EditTransactionViewEvent.OnSetTransactionType(it))
        },
        onCreateCategory = {
            viewModel.onEvent(EditTransactionViewEvent.CreateCategory(it))
        },
        onEditCategory = {
            viewModel.onEvent(EditTransactionViewEvent.EditCategory(it))
        },
        onPayPlannedPayment = {
            viewModel.onEvent(EditTransactionViewEvent.OnPayPlannedPayment)
        },
        onSave = {
            view.hideKeyboard()
            viewModel.onEvent(EditTransactionViewEvent.Save(it))
        },
        onSaveAndNew = {
            view.hideKeyboard()
            viewModel.onEvent(EditTransactionViewEvent.Save(closeScreen = false))
            nav.navigateTo(
                EditTransactionScreen(
                    initialTransactionId = null,
                    type = uiState.transactionType.toRouteType(),
                    accountId = uiState.account?.id,
                    categoryId = uiState.category?.id?.value,
                )
            )
        },
        onSetHasChanges = {
            viewModel.onEvent(EditTransactionViewEvent.SetHasChanges(it))
        },
        onDelete = {
            viewModel.onEvent(EditTransactionViewEvent.Delete)
        },
        onDuplicate = {
            viewModel.onEvent(EditTransactionViewEvent.Duplicate)
        },
        onCreateAccount = {
            viewModel.onEvent(EditTransactionViewEvent.CreateAccount(it))
        },
        onExchangeRateChange = {
            viewModel.onEvent(EditTransactionViewEvent.UpdateExchangeRate(it))
        },
        onTagOperation = {
            viewModel.onEvent(it)
        },
        onClose = nav::back,
        onAddPlannedPayment = { transactionType, amount, accountId, categoryId, title, description ->
            nav.back()
            nav.navigateTo(
                EditPlannedScreen(
                    plannedPaymentRuleId = null,
                    type = transactionType.toRouteType(),
                    amount = amount,
                    accountId = accountId,
                    categoryId = categoryId,
                    title = title,
                    description = description,
                )
            )
        },
    )
}

@Suppress("LongParameterList", "LongMethod", "CyclomaticComplexMethod")
@ExperimentalFoundationApi
@Composable
private fun BoxWithConstraintsScope.UI(
    screen: EditTransactionScreen,
    transactionType: TransactionType,
    baseCurrency: String,
    initialTitle: String?,
    titleSuggestions: ImmutableSet<String>,
    description: String?,
    category: Category?,
    dateTime: Instant?,
    account: EditTransactionAccount?,
    toAccount: EditTransactionAccount?,
    dueDate: Instant?,
    amount: Double,

    customExchangeRateState: CustomExchangeRateState,
    categories: ImmutableList<Category>,
    accounts: ImmutableList<EditTransactionAccount>,
    tags: ImmutableList<Tag>,
    transactionAssociatedTags: ImmutableList<TagId>,
    onTitleChange: (String?) -> Unit,
    onDescriptionChange: (String?) -> Unit,
    onAmountChange: (Double) -> Unit,
    onCategoryChange: (Category?) -> Unit,
    onAccountChange: (EditTransactionAccount) -> Unit,
    onToAccountChange: (EditTransactionAccount) -> Unit,
    onDueDateChange: (LocalDateTime?) -> Unit,
    onSetDate: () -> Unit,
    onSetTime: () -> Unit,
    onSetTransactionType: (TransactionType) -> Unit,

    onCreateCategory: (CreateCategoryData) -> Unit,
    onEditCategory: (Category) -> Unit,
    onPayPlannedPayment: () -> Unit,
    onSave: (closeScreen: Boolean) -> Unit,
    onSaveAndNew: () -> Unit,
    onSetHasChanges: (hasChanges: Boolean) -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onCreateAccount: (CreateAccountData) -> Unit,
    onExchangeRateChange: (Double?) -> Unit = { },
    onTagOperation: (EditTransactionViewEvent.TagEvent) -> Unit = {},
    onClose: () -> Unit,
    onAddPlannedPayment: (TransactionType, Double, UUID?, UUID?, String, String?) -> Unit,
    loanData: EditTransactionDisplayLoan = EditTransactionDisplayLoan(),
    backgroundProcessing: Boolean = false,
    hasChanges: Boolean = false,

    ) {
    var tagModelVisible by remember { mutableStateOf(false) }
    var categoryModalVisible by remember { mutableStateOf(false) }
    var categoryModalCategory: Category? by remember { mutableStateOf(null) }
    var accountModalVisible by remember { mutableStateOf(false) }
    var descriptionModalVisible by remember { mutableStateOf(false) }
    var deleteTransactionModalVisible by remember { mutableStateOf(false) }
    var changeTransactionTypeModalVisible by remember { mutableStateOf(false) }
    var amountModalShown by remember { mutableStateOf(false) }
    var exchangeRateAmountModalShown by remember { mutableStateOf(false) }
    var accountChangeModal by remember { mutableStateOf(false) }
    val waitModalVisible by remember(backgroundProcessing) {
        mutableStateOf(backgroundProcessing)
    }
    var selectedAcc by remember(account) {
        mutableStateOf(account)
    }

    val amountModalId =
        remember(screen.initialTransactionId, customExchangeRateState.exchangeRate) {
            UUID.randomUUID()
        }

    var titleTextFieldValue by remember(initialTitle) {
        mutableStateOf(
            TextFieldValue(
                initialTitle ?: ""
            )
        )
    }
    val titleFocus = FocusRequester()
    val scrollState = rememberScrollState()

    // This is to scroll the column to the customExchangeCard composable when it is shown
    var customExchangeRatePosition by remember { mutableFloatStateOf(0F) }
    LaunchedEffect(key1 = customExchangeRateState.showCard) {
        val scrollInt =
            if (customExchangeRateState.showCard) customExchangeRatePosition.roundToInt() else 0
        scrollState.animateScrollTo(scrollInt)
    }

    val isTransfer = transactionType == TransactionType.TRANSFER
    val fromLabel = when (transactionType) {
        TransactionType.INCOME -> stringResource(R.string.add_money_to)
        TransactionType.EXPENSE -> stringResource(R.string.pay_with)
        TransactionType.TRANSFER -> stringResource(R.string.from)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EditTransactionTheme.colors.pure)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .navigationBarsPadding()
            .imePadding()
    ) {
        Spacer(Modifier.height(8.dp))

        TransactionTopBar(
            title = if (screen.initialTransactionId == null) "记一笔" else "编辑交易",
            showEditActions = screen.initialTransactionId != null,
            onClose = onClose,
            onDelete = { deleteTransactionModalVisible = true },
            onDuplicate = onDuplicate,
        )

        Spacer(Modifier.height(20.dp))

        if (!loanData.isLoanRecord) {
            TransactionTypeSelector(
                selected = transactionType,
                onSelect = { onSetTransactionType(it) },
            )

            Spacer(Modifier.height(24.dp))
        }

        AmountHeader(
            amount = amount,
            currency = baseCurrency,
            onClick = { amountModalShown = true },
        )

        Spacer(Modifier.height(20.dp))

        AccountSection(
            label = fromLabel,
            accounts = accounts,
            selected = account,
            onSelect = {
                if (loanData.isLoan && account?.currency != it.currency) {
                    selectedAcc = it
                    accountChangeModal = true
                } else {
                    onAccountChange(it)
                }
            },
            onAddNewAccount = { accountModalVisible = true },
        )

        if (isTransfer) {
            Spacer(Modifier.height(16.dp))

            AccountSection(
                label = stringResource(R.string.to),
                accounts = accounts,
                selected = toAccount,
                onSelect = onToAccountChange,
                onAddNewAccount = { accountModalVisible = true },
            )

            if (customExchangeRateState.showCard) {
                Spacer(Modifier.height(16.dp))
                CustomExchangeRateCard(
                    fromCurrencyCode = baseCurrency,
                    toCurrencyCode = customExchangeRateState.toCurrencyCode ?: baseCurrency,
                    exchangeRate = customExchangeRateState.exchangeRate,
                    onRefresh = {
                        onExchangeRateChange(null)
                    },
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        customExchangeRatePosition = coordinates.positionInParent().y * 0.3f
                    }
                ) {
                    exchangeRateAmountModalShown = true
                }
            }
        }

        if (!isTransfer) {
            Spacer(Modifier.height(24.dp))

            Text(
                modifier = Modifier.padding(start = 20.dp),
                text = stringResource(R.string.categories),
                style = EditTransactionTheme.typo.nC.copy(
                    color = EditTransactionTheme.colors.mediumInverse,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                ),
            )

            Spacer(Modifier.height(12.dp))

            CategoryGrid(
                categories = categories,
                accountVisibleCategoryIds = account?.visibleCategoryIds ?: emptySet(),
                selected = category,
                onSelect = { onCategoryChange(it) },
                onAddNew = {
                    categoryModalCategory = null
                    categoryModalVisible = true
                },
            )
        }

        Spacer(Modifier.height(24.dp))

        Title(
            type = transactionType,
            titleFocus = titleFocus,
            initialTransactionId = screen.initialTransactionId,
            titleTextFieldValue = titleTextFieldValue,
            setTitleTextFieldValue = {
                titleTextFieldValue = it
            },
            suggestions = titleSuggestions,
            scrollState = scrollState,
            onTitleChanged = onTitleChange,
            onNext = {
                when {
                    shouldFocusAmount(amount = amount) -> {
                        amountModalShown = true
                    }

                    else -> {
                        onSave(true)
                    }
                }
            }
        )

        if (loanData.loanCaption != null) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                modifier = Modifier.padding(horizontal = 24.dp),
                text = loanData.loanCaption!!,
                style = EditTransactionTheme.typo.nB2.copy(
                    color = EditTransactionTheme.colors.mediumInverse,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Start
                )
            )
        }

        Spacer(Modifier.height(16.dp))

        AddTagButton(tagCount = transactionAssociatedTags.size, onClick = {
            tagModelVisible = true
        })

        Spacer(Modifier.height(16.dp))

        val datePicker = LocalDatePicker.current
        if (dueDate != null) {
            EditTransactionDueDate(dueDate = dueDate) {
                datePicker.pickDate(
                    initialDate = dueDate.toLocalDateInSystemZone(),
                    onDatePicked = {
                        onDueDateChange(it.atTime(12, 0))
                    }
                )
            }

            Spacer(Modifier.height(12.dp))
        }

        Description(
            description = description,
            onAddDescription = { descriptionModalVisible = true },
            onEditDescription = { descriptionModalVisible = true }
        )

        EditTransactionDateTime(
            dateTime = dateTime,
            dueDateTime = dueDate,
            onEditDate = onSetDate,
            onEditTime = onSetTime,
        )

        if (dueDate == null && !isTransfer && dateTime == null) {
            Spacer(Modifier.height(12.dp))

            EditTransactionAddPlannedDateButton {
                onAddPlannedPayment(
                    transactionType,
                    amount,
                    account?.id,
                    category?.id?.value,
                    titleTextFieldValue.text,
                    description
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        SaveActions(
            initialTransactionId = screen.initialTransactionId,
            transactionType = transactionType,
            dueDate = dueDate,
            hasChanges = hasChanges,
            onSave = onSave,
            onSetHasChanges = onSetHasChanges,
            onPayPlannedPayment = onPayPlannedPayment,
            onSaveAndNew = onSaveAndNew,
        )

        Spacer(Modifier.height(24.dp))
    }

    onScreenStart {
        if (screen.initialTransactionId == null) {
            amountModalShown = true
        }
    }

    val mainAmountModalId =
        remember(screen.initialTransactionId, customExchangeRateState.exchangeRate) {
            UUID.randomUUID()
        }
    AmountModal(
        id = mainAmountModalId,
        visible = amountModalShown,
        currency = baseCurrency,
        initialAmount = amount.takeIf { it > 0 },
        dismiss = { amountModalShown = false },
        onAmountChanged = {
            onAmountChange(it)
            if (shouldFocusTitle(titleTextFieldValue, transactionType)) {
                titleFocus.requestFocus()
            }
        }
    )

    // Modals
    CategoryModal(
        visible = categoryModalVisible,
        category = categoryModalCategory?.toCategoryModalCategory(),
        usedColors = categories.map { it.color.value },
        onCreateCategory = { createData ->
            onCreateCategory(createData.toCreateCategoryData())
        },
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
        account = null,
        baseCurrency = baseCurrency,
        balance = 0.0,
        usedColors = accounts.map { it.color },
        onCreateAccount = { onCreateAccount(it.toCreateAccountData()) },
        onEditAccount = { _, _ -> },
        dismiss = {
            accountModalVisible = false
        }
    )

    DescriptionModal(
        visible = descriptionModalVisible,
        description = description,
        onDescriptionChanged = onDescriptionChange,
        dismiss = {
            descriptionModalVisible = false
        }
    )

    DeleteModal(
        visible = deleteTransactionModalVisible,
        title = stringResource(R.string.confirm_deletion),
        description = stringResource(R.string.transaction_confirm_deletion_description),
        dismiss = { deleteTransactionModalVisible = false }
    ) {
        onDelete()
    }

    ChangeTransactionTypeModal(
        visible = changeTransactionTypeModalVisible,
        includeTransferType = true,
        initialType = transactionType,
        dismiss = {
            changeTransactionTypeModalVisible = false
        }
    ) {
        onSetTransactionType(it)
    }

    DeleteModal(
        visible = accountChangeModal,
        title = stringResource(R.string.confirm_account_change),
        description = stringResource(R.string.confirm_account_change_description),
        buttonText = stringResource(R.string.confirm),
        iconStart = R.drawable.ic_agreed,
        dismiss = {
            accountChangeModal = false
        }
    ) {
        selectedAcc?.let { onAccountChange(it) }
        accountChangeModal = false
    }

    ProgressModal(
        title = stringResource(R.string.confirm_account_change),
        description = stringResource(R.string.confirm_account_loan_change),
        visible = waitModalVisible
    )

    AmountModal(
        id = amountModalId,
        visible = exchangeRateAmountModalShown,
        currency = "",
        initialAmount = customExchangeRateState.exchangeRate,
        dismiss = { exchangeRateAmountModalShown = false },
        decimalCountMax = IvyCurrency.getDecimalPlaces(
            customExchangeRateState.toCurrencyCode ?: baseCurrency
        ),
        onAmountChanged = {
            onExchangeRateChange(it)
        }
    )

    ShowTagModal(
        visible = tagModelVisible,
        onDismiss = {
            tagModelVisible = false
        },
        allTagList = tags.map { it.toTagModalTag() }.toImmutableList(),
        selectedTagList = transactionAssociatedTags.map { it.value }.toImmutableList(),
        onTagAdd = {
            onTagOperation(EditTransactionViewEvent.TagEvent.SaveTag(name = it))
        },
        onTagEdit = { tagId, name ->
            val updatedTag = tags.firstOrNull { it.id.value == tagId }?.copy(
                name = NotBlankTrimmedString.unsafe(name)
            )
            if (updatedTag != null) {
                onTagOperation(EditTransactionViewEvent.TagEvent.OnTagEdit(updatedTag))
            }
        },
        onTagDelete = {
            onTagOperation(EditTransactionViewEvent.TagEvent.OnTagDelete(TagId(it)))
        },
        onTagSelected = {
            onTagOperation(EditTransactionViewEvent.TagEvent.OnTagSelect(TagId(it)))
        },
        onTagDeSelected = {
            onTagOperation(EditTransactionViewEvent.TagEvent.OnTagDeSelect(TagId(it)))
        }
    )
}

private fun shouldFocusTitle(
    titleTextFieldValue: TextFieldValue,
    type: TransactionType
): Boolean = titleTextFieldValue.text.isBlank() && type != TransactionType.TRANSFER

private fun shouldFocusAmount(amount: Double) = amount == 0.0

private fun TransactionType.toRouteType(): TransactionRouteType {
    return TransactionRouteType.valueOf(name)
}

private fun TransactionRouteType.toTransactionType(): TransactionType {
    return TransactionType.valueOf(name)
}

private fun Tag.toTagModalTag() = TagModalTag(
    id = id.value,
    name = name.value,
)

private fun Category.toCategoryModalCategory() = CategoryModalCategory(
    id = id.value,
    name = name.value,
    color = color.value,
    icon = icon?.id,
)

private fun CategoryModalSaveData.toCreateCategoryData() = CreateCategoryData(
    name = name,
    color = color,
    icon = icon,
)

private fun AccountModalSaveData.toCreateAccountData() = CreateAccountData(
    name = name,
    currency = currency,
    color = color,
    icon = icon,
    balance = balance,
    includeBalance = includeInBalance,
)

private fun Category.withModalSaveData(data: CategoryModalSaveData) = copy(
    name = NotBlankTrimmedString.unsafe(data.name),
    color = ColorInt(data.color),
    icon = data.icon?.let { IconAsset.unsafe(it) },
)

private fun Instant.toLocalDateInSystemZone() =
    atZone(ZoneId.systemDefault()).toLocalDate()

@Composable
private fun EditTransactionAddPlannedDateButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(EditTransactionTheme.shapes.r4)
            .background(EditTransactionTheme.colors.medium, EditTransactionTheme.shapes.r4)
            .clickable(onClick = onClick)
            .padding(vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(16.dp))

        ResourceIcon(
            icon = R.drawable.ic_planned_payments,
            tint = EditTransactionTheme.colors.pureInverse
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = stringResource(R.string.add_planned_date_payment),
            style = EditTransactionTheme.typo.b2.copy(
                color = EditTransactionTheme.colors.pureInverse,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start
            )
        )
    }
}

@Composable
private fun TransactionTopBar(
    title: String,
    showEditActions: Boolean,
    onClose: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onClose)
                .padding(8.dp),
            painter = painterResource(R.drawable.ic_back),
            contentDescription = "back",
            tint = EditTransactionTheme.colors.pureInverse,
        )

        Spacer(Modifier.weight(1f))

        Text(
            text = title,
            style = EditTransactionTheme.typo.b1.copy(
                color = EditTransactionTheme.colors.pureInverse,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            ),
        )

        Spacer(Modifier.weight(1f))

        if (showEditActions) {
            Icon(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onDuplicate)
                    .padding(8.dp)
                    .size(22.dp),
                imageVector = Icons.Sharp.CopyAll,
                contentDescription = "duplicate",
                tint = EditTransactionTheme.colors.pureInverse,
            )

            Icon(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onDelete)
                    .padding(8.dp)
                    .size(22.dp),
                painter = painterResource(R.drawable.ic_delete),
                contentDescription = "delete",
                tint = EditTransactionTheme.colors.red,
            )
        } else {
            Spacer(Modifier.width(40.dp))
        }
    }
}

@Composable
private fun TransactionTypeSelector(
    selected: TransactionType,
    onSelect: (TransactionType) -> Unit,
) {
    val options = listOf(
        TransactionType.EXPENSE to stringResource(R.string.expense),
        TransactionType.INCOME to stringResource(R.string.income),
        TransactionType.TRANSFER to stringResource(R.string.transfer),
    )
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(EditTransactionTheme.shapes.rFull)
            .background(EditTransactionTheme.colors.medium)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { (type, label) ->
            val isSel = type == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(EditTransactionTheme.shapes.rFull)
                    .background(
                        if (isSel) EditTransactionTheme.colors.pureInverse else Color.Transparent
                    )
                    .clickable { if (!isSel) onSelect(type) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = EditTransactionTheme.typo.b2.copy(
                        color = if (isSel) {
                            EditTransactionTheme.colors.pure
                        } else {
                            EditTransactionTheme.colors.pureInverse
                        },
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    ),
                )
            }
        }
    }
}

@Composable
private fun AmountHeader(
    amount: Double,
    currency: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        BalanceRow(
            modifier = Modifier
                .clickableNoIndication(rememberInteractionSource()) { onClick() }
                .testTag("edit_amount_balance_row"),
            currency = currency,
            balance = amount,
            spacerCurrency = 8.dp,
            balanceFontSize = 44.sp,
            currencyFontSize = 22.sp,
            currencyUpfront = false,
        )
    }
}

@Composable
private fun AccountSection(
    label: String,
    accounts: ImmutableList<EditTransactionAccount>,
    selected: EditTransactionAccount?,
    onSelect: (EditTransactionAccount) -> Unit,
    onAddNewAccount: () -> Unit,
) {
    Column {
        Text(
            modifier = Modifier.padding(start = 20.dp),
            text = label,
            style = EditTransactionTheme.typo.nC.copy(
                color = EditTransactionTheme.colors.mediumInverse,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
            ),
        )

        Spacer(Modifier.height(8.dp))

        AccountsRow(
            accounts = accounts,
            selectedAccount = selected,
            onSelectedAccountChanged = onSelect,
            onAddNewAccount = onAddNewAccount,
        )
    }
}

@Composable
private fun AccountsRow(
    accounts: List<EditTransactionAccount>,
    selectedAccount: EditTransactionAccount?,
    onSelectedAccountChanged: (EditTransactionAccount) -> Unit,
    onAddNewAccount: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lazyState = rememberLazyListState()

    LaunchedEffect(accounts, selectedAccount) {
        if (selectedAccount != null) {
            val selectedIndex = accounts.indexOf(selectedAccount)
            if (selectedIndex != -1) {
                launch {
                    lazyState.scrollToItem(index = selectedIndex)
                }
            }
        }
    }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        state = lazyState
    ) {
        item {
            Spacer(Modifier.width(16.dp))
        }

        itemsIndexed(accounts) { _, account ->
            AccountChip(
                account = account,
                selected = selectedAccount == account,
            ) {
                onSelectedAccountChanged(account)
            }
            Spacer(Modifier.width(8.dp))
        }

        item {
            AddAccount {
                onAddNewAccount()
            }
        }

        item {
            Spacer(Modifier.width(16.dp))
        }
    }
}

@Composable
private fun AccountChip(
    account: EditTransactionAccount,
    selected: Boolean,
    onClick: () -> Unit
) {
    val accountColor = account.color.toComposeColor()
    val textColor =
        if (selected) findContrastTextColor(accountColor) else EditTransactionTheme.colors.pureInverse

    val medium = EditTransactionTheme.colors.medium
    val rFull = EditTransactionTheme.shapes.rFull

    Row(
        modifier = Modifier
            .clip(rFull)
            .thenIf(!selected) {
                border(2.dp, medium, rFull)
            }
            .thenIf(selected) {
                background(accountColor, rFull)
            }
            .clickable(onClick = onClick)
            .testTag("account")
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ItemIconSDefaultIcon(
            iconName = account.icon,
            defaultIcon = R.drawable.ic_custom_account_s,
            tint = textColor
        )

        Spacer(Modifier.width(6.dp))

        Text(
            text = account.name,
            style = EditTransactionTheme.typo.b2.copy(
                color = textColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start
            )
        )
    }
}

@Composable
private fun AddAccount(
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(EditTransactionTheme.shapes.rFull)
            .border(2.dp, EditTransactionTheme.colors.medium, EditTransactionTheme.shapes.rFull)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ResourceIcon(
            icon = R.drawable.ic_plus,
            tint = EditTransactionTheme.colors.pureInverse
        )

        Spacer(Modifier.width(6.dp))

        Text(
            text = stringResource(R.string.add_account),
            style = EditTransactionTheme.typo.b2.copy(
                color = EditTransactionTheme.colors.pureInverse,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start
            )
        )
    }
}

@Composable
private fun CategoryGrid(
    categories: ImmutableList<Category>,
    accountVisibleCategoryIds: Set<UUID>,
    selected: Category?,
    onSelect: (Category?) -> Unit,
    onAddNew: () -> Unit,
) {
    // The account's own categories (plus whatever is currently selected, so editing an old
    // transaction never hides its category). Everything else is tucked behind "全部类别".
    val belonging = remember(categories, accountVisibleCategoryIds, selected) {
        categories.filter {
            it.id.value in accountVisibleCategoryIds || it.id == selected?.id
        }
    }
    val others = remember(categories, belonging) {
        val belongingIds = belonging.mapTo(HashSet()) { it.id }
        categories.filter { it.id !in belongingIds }
    }
    var showAll by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        CategoryCellRows(
            items = belonging,
            includeAddCell = true,
            selected = selected,
            onSelect = onSelect,
            onAddNew = onAddNew,
        )

        if (others.isNotEmpty()) {
            ShowAllCategoriesToggle(
                expanded = showAll,
                onToggle = { showAll = !showAll },
            )

            if (showAll) {
                Spacer(Modifier.height(12.dp))
                CategoryCellRows(
                    items = others,
                    includeAddCell = false,
                    selected = selected,
                    onSelect = onSelect,
                    onAddNew = onAddNew,
                )
            }
        }
    }
}

@Composable
private fun CategoryCellRows(
    items: List<Category>,
    includeAddCell: Boolean,
    selected: Category?,
    onSelect: (Category?) -> Unit,
    onAddNew: () -> Unit,
) {
    val cells: List<Category?> = if (includeAddCell) items + listOf<Category?>(null) else items
    if (cells.isEmpty()) return
    cells.chunked(4).forEach { rowItems ->
        Row(modifier = Modifier.fillMaxWidth()) {
            rowItems.forEach { cat ->
                if (cat == null) {
                    AddCategoryCell(
                        modifier = Modifier.weight(1f),
                        onClick = onAddNew,
                    )
                } else {
                    CategoryCell(
                        modifier = Modifier.weight(1f),
                        category = cat,
                        selected = selected?.id == cat.id,
                        onClick = {
                            onSelect(if (selected?.id == cat.id) null else cat)
                        },
                    )
                }
            }
            repeat(4 - rowItems.size) {
                Spacer(Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ShowAllCategoriesToggle(
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(EditTransactionTheme.shapes.r4)
            .clickable(onClick = onToggle)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (expanded) "收起其他类别" else "全部类别",
            style = EditTransactionTheme.typo.nC.copy(
                color = EditTransactionTheme.colors.mediumInverse,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
            ),
        )

        Spacer(Modifier.width(6.dp))

        ResourceIcon(
            modifier = Modifier
                .size(16.dp)
                .rotate(if (expanded) 180f else 0f),
            icon = R.drawable.ic_expandarrow,
            tint = EditTransactionTheme.colors.mediumInverse,
        )
    }
}

@Composable
private fun CategoryCell(
    category: Category,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val categoryColor = category.color.value.toComposeColor()
    Column(
        modifier = modifier
            .clip(EditTransactionTheme.shapes.r4)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(categoryColor.copy(alpha = if (selected) 0.25f else 0.12f))
                .thenIf(selected) {
                    border(2.dp, categoryColor, CircleShape)
                },
            contentAlignment = Alignment.Center,
        ) {
            ItemIconSDefaultIcon(
                iconName = category.icon?.id,
                defaultIcon = R.drawable.ic_custom_category_s,
                tint = categoryColor,
            )
        }

        Spacer(Modifier.height(6.dp))

        Text(
            modifier = Modifier.padding(horizontal = 2.dp),
            text = category.name.value,
            maxLines = 1,
            style = EditTransactionTheme.typo.nC.copy(
                color = if (selected) categoryColor else EditTransactionTheme.colors.pureInverse,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center,
            ),
        )
    }
}

@Composable
private fun AddCategoryCell(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(EditTransactionTheme.shapes.r4)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .border(2.dp, EditTransactionTheme.colors.medium, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            ResourceIcon(
                icon = R.drawable.ic_plus,
                tint = EditTransactionTheme.colors.mediumInverse,
            )
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = stringResource(R.string.add_category),
            maxLines = 1,
            style = EditTransactionTheme.typo.nC.copy(
                color = EditTransactionTheme.colors.mediumInverse,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            ),
        )
    }
}

@Composable
private fun SaveActions(
    initialTransactionId: UUID?,
    transactionType: TransactionType,
    dueDate: Instant?,
    hasChanges: Boolean,
    onSave: (Boolean) -> Unit,
    onSetHasChanges: (Boolean) -> Unit,
    onPayPlannedPayment: () -> Unit,
    onSaveAndNew: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        when {
            initialTransactionId == null -> {
                PrimaryActionButton(text = stringResource(R.string.save)) { onSave(true) }
                Spacer(Modifier.height(12.dp))
                SecondaryActionButton(text = "保存并再记一笔") { onSaveAndNew() }
            }

            dueDate != null && !hasChanges -> {
                PrimaryActionButton(
                    text = if (transactionType == TransactionType.EXPENSE) {
                        stringResource(R.string.pay)
                    } else {
                        stringResource(R.string.get)
                    },
                    iconStart = R.drawable.ic_check,
                    gradient = IvyGradients.Green,
                ) { onPayPlannedPayment() }
            }

            dueDate != null && hasChanges -> {
                PrimaryActionButton(text = stringResource(R.string.save)) {
                    onSave(false)
                    onSetHasChanges(false)
                }
            }

            else -> {
                PrimaryActionButton(text = stringResource(R.string.save)) { onSave(true) }
            }
        }
    }
}

@Composable
private fun PrimaryActionButton(
    text: String,
    modifier: Modifier = Modifier,
    @DrawableRes iconStart: Int? = null,
    gradient: Gradient = IvyGradients.Mint,
    onClick: () -> Unit,
) {
    GradientButton(
        modifier = modifier.fillMaxWidth(),
        text = text,
        iconStart = iconStart,
        backgroundGradient = gradient,
        disabledBackgroundColor = EditTransactionTheme.colors.gray,
        shape = EditTransactionTheme.shapes.rFull,
        textStyle = EditTransactionTheme.typo.b1.copy(
            color = Color(0xFFFAFAFA),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        ),
        iconTint = Color(0xFFFAFAFA),
        wrapContentMode = false,
        hasGlow = false,
        padding = 16.dp,
        onClick = onClick,
    )
}

@Composable
private fun SecondaryActionButton(
    text: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(EditTransactionTheme.shapes.rFull)
            .border(2.dp, EditTransactionTheme.colors.medium, EditTransactionTheme.shapes.rFull)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = EditTransactionTheme.typo.b2.copy(
                color = IvyGradients.Mint.startColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            ),
        )
    }
}
