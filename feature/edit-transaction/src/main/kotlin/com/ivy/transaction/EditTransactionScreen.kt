package com.ivy.transaction

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.data.model.TransactionType
import com.ivy.data.model.Category
import com.ivy.data.model.Tag
import com.ivy.data.model.TagId
import com.ivy.ui.platform.LocalDatePicker
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.ui.platform.hideKeyboard
import com.ivy.legacy.ui.tags.ShowTagModal
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
import com.ivy.ui.modal.ModalAdd
import com.ivy.ui.modal.ModalSave
import com.ivy.ui.modal.ProgressModal
import com.ivy.legacy.ui.modal.edit.AccountModal
import com.ivy.legacy.ui.modal.edit.AmountModal
import com.ivy.legacy.ui.modal.edit.CategoryModal
import com.ivy.legacy.ui.modal.edit.ChooseCategoryModal
import com.ivy.ui.compose.GradientButton
import com.ivy.ui.compose.ResourceIcon
import com.ivy.ui.theme.colors.IvyGradients
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
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
    var chooseCategoryModalVisible by remember { mutableStateOf(false) }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
    ) {
        Spacer(Modifier.height(16.dp))

        Toolbar(
            // Setting the transaction type to TransactionType.TRANSFER for transactions associated
            // with loan record to hide the ChangeTransactionType Button
            type = if (loanData.isLoanRecord) TransactionType.TRANSFER else transactionType,
            initialTransactionId = screen.initialTransactionId,
            onClose = onClose,
            onDeleteTransactionModal = {
                deleteTransactionModalVisible = true
            },
            onChangeTransactionTypeModal = {
                changeTransactionTypeModalVisible = true
            },
            showDuplicateButton = true,
            onDuplicate = onDuplicate
        )

        Spacer(Modifier.height(32.dp))

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
                style = LegacyTheme.typo.nB2.copy(
                    color = LegacyTheme.colors.mediumInverse,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Start
                )
            )
        }

        Spacer(Modifier.height(32.dp))

        Category(category = category, onChooseCategory = {
            chooseCategoryModalVisible = true
        })

        Spacer(Modifier.height(16.dp))

        AddTagButton(transactionAssociatedTags = transactionAssociatedTags, onClick = {
            tagModelVisible = true
        })

        Spacer(Modifier.height(32.dp))

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

        if (transactionType == TransactionType.TRANSFER && customExchangeRateState.showCard) {
            Spacer(Modifier.height(12.dp))
            CustomExchangeRateCard(
                fromCurrencyCode = baseCurrency,
                toCurrencyCode = customExchangeRateState.toCurrencyCode ?: baseCurrency,
                exchangeRate = customExchangeRateState.exchangeRate,
                onRefresh = {
                    // Set exchangeRate to null to reset
                    onExchangeRateChange(null)
                },
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    customExchangeRatePosition = coordinates.positionInParent().y * 0.3f
                }
            ) {
                exchangeRateAmountModalShown = true
            }
        }

        if (dueDate == null && transactionType != TransactionType.TRANSFER && dateTime == null) {
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

        Spacer(Modifier.height(600.dp)) // scroll hack
    }

    onScreenStart {
        if (screen.initialTransactionId == null) {
            amountModalShown = true
        }
    }

    EditBottomSheet(
        initialTransactionId = screen.initialTransactionId,
        type = transactionType,
        accounts = accounts,
        selectedAccount = account,
        toAccount = toAccount,
        amount = amount,
        currency = baseCurrency,
        convertedAmount = customExchangeRateState.convertedAmount,
        convertedAmountCurrencyCode = customExchangeRateState.toCurrencyCode,

        ActionButton = {
            if (screen.initialTransactionId != null) {
                // Edit mode
                if (dueDate != null) {
                    // due date stuff
                    if (hasChanges) {
                        // has changes
                        ModalSave {
                            onSave(false)
                            onSetHasChanges(false)
                        }
                    } else {
                        // no changes, pay
                        PayOrGetPlannedButton(
                            label = if (transactionType == TransactionType.EXPENSE) {
                                stringResource(
                                    R.string.pay
                                )
                            } else {
                                stringResource(R.string.get)
                            }
                        ) {
                            onPayPlannedPayment()
                        }
                    }
                } else {
                    // normal transaction
                    ModalSave {
                        onSave(true)
                    }
                }
            } else {
                // create new mode
                ModalAdd {
                    onSave(true)
                }
            }
        },

        amountModalShown = amountModalShown,
        setAmountModalShown = {
            amountModalShown = it
        },

        onAmountChanged = {
            onAmountChange(it)
            if (shouldFocusCategory(category)) {
                chooseCategoryModalVisible = true
            } else if (shouldFocusTitle(titleTextFieldValue, transactionType)) {
                titleFocus.requestFocus()
            }
        },
        onSelectedAccountChanged = {
            if (loanData.isLoan && account?.currency != it.currency) {
                selectedAcc = it
                accountChangeModal = true
            } else {
                onAccountChange(it)
            }
        },
        onToAccountChanged = onToAccountChange,
        onAddNewAccount = {
            accountModalVisible = true
        }
    )

    // Modals
    ChooseCategoryModal(
        visible = chooseCategoryModalVisible,
        initialCategory = category,
        categories = categories,
        showCategoryModal = {
            categoryModalCategory = it
            categoryModalVisible = true
        },
        onCategoryChanged = {
            onCategoryChange(it)
            if (shouldFocusTitle(titleTextFieldValue, transactionType)) {
                titleFocus.requestFocus()
            } else if (shouldFocusAmount(amount = amount)) {
                amountModalShown = true
            }
        },
        dismiss = {
            chooseCategoryModalVisible = false
        }
    )

    CategoryModal(
        visible = categoryModalVisible,
        category = categoryModalCategory,
        onCreateCategory = { createData ->
            onCreateCategory(createData)
            chooseCategoryModalVisible = false
        },
        onEditCategory = onEditCategory,
        dismiss = {
            categoryModalVisible = false
        }
    )

    AccountModal(
        visible = accountModalVisible,
        account = null,
        baseCurrency = baseCurrency,
        balance = 0.0,
        onCreateAccount = onCreateAccount,
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
            // Reset TagList, avoids showing incorrect tag list when user has searched for a tag
            onTagOperation(EditTransactionViewEvent.TagEvent.OnTagSearch(""))
        },
        allTagList = tags,
        selectedTagList = transactionAssociatedTags,
        onTagAdd = {
            onTagOperation(EditTransactionViewEvent.TagEvent.SaveTag(name = it))
        },
        onTagEdit = { _, newTag ->
            onTagOperation(EditTransactionViewEvent.TagEvent.OnTagEdit(newTag))
        },
        onTagDelete = {
            onTagOperation(EditTransactionViewEvent.TagEvent.OnTagDelete(it.id))
        },
        onTagSelected = {
            onTagOperation(EditTransactionViewEvent.TagEvent.OnTagSelect(it.id))
        },
        onTagDeSelected = {
            onTagOperation(EditTransactionViewEvent.TagEvent.OnTagDeSelect(it.id))
        },
        onTagSearch = {
            onTagOperation(EditTransactionViewEvent.TagEvent.OnTagSearch(it))
        }
    )
}

private fun shouldFocusCategory(
    category: Category?,
): Boolean = category == null

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

private fun Instant.toLocalDateInSystemZone() =
    atZone(ZoneId.systemDefault()).toLocalDate()

@Composable
private fun EditTransactionAddPlannedDateButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(LegacyTheme.shapes.r4)
            .background(LegacyTheme.colors.medium, LegacyTheme.shapes.r4)
            .clickable(onClick = onClick)
            .padding(vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(16.dp))

        ResourceIcon(
            icon = R.drawable.ic_planned_payments,
            tint = LegacyTheme.colors.pureInverse
        )

        Spacer(Modifier.width(8.dp))

        Text(
            text = stringResource(R.string.add_planned_date_payment),
            style = LegacyTheme.typo.b2.copy(
                color = LegacyTheme.colors.pureInverse,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start
            )
        )
    }
}

@Composable
private fun PayOrGetPlannedButton(
    label: String,
    onClick: () -> Unit,
) {
    GradientButton(
        text = label,
        backgroundGradient = IvyGradients.Green,
        disabledBackgroundColor = LegacyTheme.colors.gray,
        shape = LegacyTheme.shapes.rFull,
        textStyle = LegacyTheme.typo.b2.copy(
            color = Color(0xFFFAFAFA),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start
        ),
        iconStart = R.drawable.ic_check,
        iconTint = Color(0xFFFAFAFA),
        onClick = onClick
    )
}
