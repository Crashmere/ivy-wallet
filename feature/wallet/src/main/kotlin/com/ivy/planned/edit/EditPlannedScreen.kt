package com.ivy.planned.edit

import com.ivy.planned.PlannedTheme

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.data.model.TransactionType
import com.ivy.data.model.Category
import com.ivy.data.model.CategoryId
import com.ivy.data.model.CreateAccountData
import com.ivy.data.model.CreateCategoryData
import com.ivy.data.model.IntervalType
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.IconAsset
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.ui.platform.LocalDatePicker
import com.ivy.ui.navigation.onScreenStart
import com.ivy.ui.navigation.EditPlannedScreen
import com.ivy.ui.navigation.TransactionRouteType
import com.ivy.ui.navigation.navigation
import com.ivy.ui.navigation.screenScopedViewModel
import com.ivy.ui.R
import com.ivy.ui.modal.DeleteModal
import com.ivy.ui.modal.AccountModal
import com.ivy.ui.modal.AccountModalSaveData
import com.ivy.ui.modal.CategoryModal
import com.ivy.ui.modal.CategoryModalCategory
import com.ivy.ui.modal.CategoryModalSaveData
import com.ivy.ui.modal.ChooseCategoryModal
import com.ivy.ui.compose.GradientButton
import com.ivy.ui.theme.colors.IvyGradients
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDateTime

@ExperimentalFoundationApi
@Composable
fun BoxWithConstraintsScope.EditPlannedScreen(screen: EditPlannedScreen) {
    val viewModel: EditPlannedViewModel = screenScopedViewModel()
    val nav = navigation()
    val uiState = viewModel.uiState()

    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                EditPlannedUiEvent.CloseScreen -> nav.back()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.start(
            plannedPaymentRuleId = screen.plannedPaymentRuleId,
            type = screen.type.toTransactionType(),
            amount = screen.amount,
            accountId = screen.accountId,
            categoryId = screen.categoryId,
            title = screen.title,
            description = screen.description
        )
    }

    UI(
        screen = screen,
        state = uiState,
        onEvent = viewModel::onEvent,
        onClose = nav::back,
    )
}

/**
 * Flow Empty: Type -> Amount -> Category -> Recurring Rule -> Title
 * Flow Amount + Category: Recurring Rule -> Title
 */
@Suppress("LongMethod")
@ExperimentalFoundationApi
@Composable
private fun BoxWithConstraintsScope.UI(
    screen: EditPlannedScreen,
    state: EditPlannedScreenState,
    onEvent: (EditPlannedScreenEvent) -> Unit,
    onClose: () -> Unit,
) {
    var titleTextFieldValue by remember(state.initialTitle) {
        mutableStateOf(
            TextFieldValue(
                state.initialTitle.orEmpty()
            )
        )
    }
    val titleFocus = FocusRequester()
    var categoryModalVisible by remember { mutableStateOf(false) }
    var categoryModalCategory: Category? by remember { mutableStateOf(null) }
    var accountModalVisible by remember { mutableStateOf(false) }
    var recurringRuleModalVisible by remember { mutableStateOf(false) }

    fun showRecurringRuleModal() {
        recurringRuleModalVisible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(16.dp))

        Toolbar(
            type = state.transactionType,
            initialTransactionId = screen.plannedPaymentRuleId,
            onClose = onClose,
            onDeleteTransactionModal = {
                onEvent(EditPlannedScreenEvent.OnDeleteTransactionModalVisible(true))
            },
            onChangeTransactionTypeModal = {
                onEvent(EditPlannedScreenEvent.OnTransactionTypeModalVisible(true))
            },
            showDuplicateButton = false,
            onDuplicate = {}
        )

        Spacer(Modifier.height(32.dp))

        Title(
            type = state.transactionType,
            titleFocus = titleFocus,
            initialTransactionId = screen.plannedPaymentRuleId,

            titleTextFieldValue = titleTextFieldValue,
            setTitleTextFieldValue = {
                titleTextFieldValue = it
            },
            suggestions = emptySet(), // DO NOT display title suggestions for "Planned Payments"
            onTitleChanged = { onEvent(EditPlannedScreenEvent.OnTitleChanged(it)) },
            onNext = {
                when {
                    shouldFocusRecurring(
                        state.startDate,
                        state.intervalN,
                        state.intervalType,
                        state.oneTime
                    ) -> {
                        showRecurringRuleModal()
                    }

                    else -> {
                        onEvent(EditPlannedScreenEvent.OnSave())
                    }
                }
            }
        )

        if (state.transactionType != TransactionType.TRANSFER) {
            Spacer(Modifier.height(32.dp))

            Category(
                category = state.category,
                onChooseCategory = {
                    onEvent(EditPlannedScreenEvent.OnCategoryModalVisible(true))
                }
            )
        }

        Spacer(Modifier.height(32.dp))

        RecurringRule(
            startDate = state.startDate,
            intervalN = state.intervalN,
            intervalType = state.intervalType,
            oneTime = state.oneTime,
            onShowRecurringRuleModal = {
                showRecurringRuleModal()
            }
        )

        Spacer(Modifier.height(12.dp))

        Description(
            description = state.description,
            onAddDescription = { onEvent(EditPlannedScreenEvent.OnDescriptionModalVisible(true)) },
            onEditDescription = { onEvent(EditPlannedScreenEvent.OnDescriptionModalVisible(true)) }
        )

        Spacer(Modifier.height(600.dp)) // scroll hack
    }

    onScreenStart {
        if (screen.plannedPaymentRuleId == null) {
            // Create mode
            if (screen.hasMandatoryInitialData()) {
                // Flow Convert (Amount, Account, Category)
                showRecurringRuleModal()
            } else {
                // Flow Empty
                onEvent(EditPlannedScreenEvent.OnTransactionTypeModalVisible(true))
            }
        }
    }

    EditBottomSheet(
        initialTransactionId = screen.plannedPaymentRuleId,
        type = state.transactionType,
        accounts = state.accounts,
        selectedAccount = state.account,
        toAccount = null,
        amount = state.amount,
        currency = state.currency,

        ActionButton = {
            EditPlannedSetButton(
                modifier = Modifier.testTag("editPlannedScreen_set")
            ) {
                onEvent(EditPlannedScreenEvent.OnSave())
            }
        },

        amountModalShown = state.amountModalVisible,
        setAmountModalShown = {
            onEvent(EditPlannedScreenEvent.OnAmountModalVisible(it))
        },

        onAmountChanged = {
            onEvent(EditPlannedScreenEvent.OnAmountChanged(it))
            when {
                shouldFocusCategory(state.category, state.transactionType) -> {
                    onEvent(EditPlannedScreenEvent.OnCategoryModalVisible(true))
                }

                shouldFocusRecurring(
                    state.startDate,
                    state.intervalN,
                    state.intervalType,
                    state.oneTime
                ) -> {
                    showRecurringRuleModal()
                }

                shouldFocusTitle(titleTextFieldValue, state.transactionType) -> {
                    titleFocus.requestFocus()
                }
            }
        },
        onSelectedAccountChanged = { onEvent(EditPlannedScreenEvent.OnAccountChanged(it.id)) },
        onToAccountChanged = { },
        onAddNewAccount = {
            accountModalVisible = true
        }
    )

    // Modals
    ChooseCategoryModal(
        visible = state.categoryModalVisible,
        initialCategoryId = state.category?.id?.value,
        categories = state.categories.map { it.toCategoryModalCategory() },
        showCategoryModal = { categoryId ->
            categoryModalCategory = state.categories.firstOrNull { it.id.value == categoryId }
            categoryModalVisible = true
        },
        onCategoryChanged = { categoryId ->
            onEvent(EditPlannedScreenEvent.OnCategoryChanged(categoryId?.let(::CategoryId)))
            showRecurringRuleModal()
        },
        dismiss = {
            onEvent(EditPlannedScreenEvent.OnCategoryModalVisible(false))
        }
    )

    CategoryModal(
        visible = categoryModalVisible,
        category = categoryModalCategory?.toCategoryModalCategory(),
        usedColors = state.categories.map { it.color.value },
        onCreateCategory = {
            onEvent(EditPlannedScreenEvent.OnCreateCategory(it.toCreateCategoryData()))
        },
        onEditCategory = { _, data ->
            val editedCategory = categoryModalCategory?.withModalSaveData(data)
            if (editedCategory != null) {
                onEvent(EditPlannedScreenEvent.OnEditCategory(editedCategory))
            }
        },
        dismiss = {
            categoryModalVisible = false
        }
    )

    AccountModal(
        visible = accountModalVisible,
        account = null,
        baseCurrency = state.currency,
        balance = 0.0,
        usedColors = state.accounts.map { it.color },
        onCreateAccount = {
            onEvent(EditPlannedScreenEvent.OnCreateAccount(it.toCreateAccountData()))
        },
        onEditAccount = { _, _ -> },
        dismiss = {
            accountModalVisible = false
        }
    )

    DescriptionModal(
        visible = state.descriptionModalVisible,
        description = state.description,
        onDescriptionChanged = { onEvent(EditPlannedScreenEvent.OnDescriptionChanged(it)) },
        dismiss = {
            onEvent(EditPlannedScreenEvent.OnDescriptionModalVisible(false))
        }
    )

    DeleteModal(
        visible = state.deleteTransactionModalVisible,
        title = stringResource(R.string.confirm_deletion),
        description = stringResource(R.string.planned_payment_confirm_deletion_description),
        dismiss = { onEvent(EditPlannedScreenEvent.OnDeleteTransactionModalVisible(false)) }
    ) {
        onEvent(EditPlannedScreenEvent.OnDelete)
    }

    ChangeTransactionTypeModal(
        title = stringResource(R.string.set_payment_type),
        visible = state.transactionTypeModalVisible,
        includeTransferType = false,
        initialType = state.transactionType,
        dismiss = {
            onEvent(EditPlannedScreenEvent.OnTransactionTypeModalVisible(false))
        }
    ) {
        onEvent(EditPlannedScreenEvent.OnSetTransactionType(it))
        if (shouldFocusAmount(state.amount)) {
            onEvent(EditPlannedScreenEvent.OnAmountModalVisible(true))
        }
    }

    val datePicker = LocalDatePicker.current
    RecurringRuleModal(
        visible = recurringRuleModalVisible,
        initialStartDate = state.startDate,
        initialIntervalN = state.intervalN,
        initialIntervalType = state.intervalType,
        initialOneTime = state.oneTime,
        pickDate = { initialDate, onDatePicked ->
            datePicker.pickDate(
                initialDate = initialDate,
                onDatePicked = {
                    onDatePicked(it.atTime(12, 0))
                }
            )
        },
        onRuleChanged = { newStartDate, newOneTime, newIntervalN, newIntervalType ->
            onEvent(
                EditPlannedScreenEvent.OnRuleChanged(
                    newStartDate,
                    newOneTime,
                    newIntervalN,
                    newIntervalType
                )
            )

            when {
                shouldFocusCategory(state.category, state.transactionType) -> {
                    onEvent(EditPlannedScreenEvent.OnCategoryModalVisible(true))
                }

                shouldFocusTitle(titleTextFieldValue, state.transactionType) -> {
                    titleFocus.requestFocus()
                }
            }
        },
        dismiss = {
            recurringRuleModalVisible = false
        }
    )
}

@Composable
private fun EditPlannedSetButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GradientButton(
        modifier = modifier,
        text = stringResource(R.string.set),
        backgroundGradient = IvyGradients.Green,
        disabledBackgroundColor = PlannedTheme.colors.gray,
        shape = PlannedTheme.shapes.rFull,
        textStyle = PlannedTheme.typo.b2.copy(
            color = Color(0xFFFAFAFA),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start
        ),
        iconStart = R.drawable.ic_check,
        iconTint = Color(0xFFFAFAFA),
        onClick = onClick
    )
}

private fun shouldFocusCategory(
    category: Category?,
    type: TransactionType,
): Boolean = category == null && type != TransactionType.TRANSFER

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

private fun EditPlannedScreen.hasMandatoryInitialData(): Boolean {
    val initialAmount = amount
    return initialAmount != null && initialAmount > 0.0 &&
            accountId != null
}

private fun TransactionRouteType.toTransactionType(): TransactionType {
    return TransactionType.valueOf(name)
}

private fun shouldFocusTitle(
    titleTextFieldValue: TextFieldValue,
    type: TransactionType,
): Boolean = titleTextFieldValue.text.isBlank() && type != TransactionType.TRANSFER

private fun shouldFocusRecurring(
    startDate: LocalDateTime?,
    intervalN: Int?,
    intervalType: IntervalType?,
    oneTime: Boolean,
): Boolean {
    return !hasRecurringRule(
        startDate = startDate,
        intervalN = intervalN,
        intervalType = intervalType,
        oneTime = oneTime
    )
}

private fun shouldFocusAmount(amount: Double) = amount == 0.0
