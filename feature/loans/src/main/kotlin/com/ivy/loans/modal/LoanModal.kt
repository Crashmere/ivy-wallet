package com.ivy.loans.modal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.data.model.LoanType
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.ui.time.LocalTimeConverter
import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.data.model.Loan
import com.ivy.data.model.currency.getDefaultFIATCurrency
import com.ivy.ui.compose.onCompositionStart
import com.ivy.ui.compose.selectEndTextFieldValue
import com.ivy.ui.compose.thenIf
import com.ivy.ui.R
import com.ivy.data.model.currency.IvyCurrency
import com.ivy.data.model.CreateAccountData
import com.ivy.data.model.CreateLoanData
import com.ivy.ui.theme.colors.IvyGradients
import com.ivy.ui.theme.colors.IvyFixedColors.Ivy
import com.ivy.ui.theme.colors.IvyFixedColors.White
import com.ivy.legacy.ui.modal.AccountModalData
import com.ivy.legacy.ui.modal.ChooseIconModal
import com.ivy.legacy.ui.modal.CurrencyModal
import com.ivy.legacy.ui.modal.DeleteModal
import com.ivy.legacy.ui.modal.IvyModal
import com.ivy.legacy.ui.modal.ModalAmountSection
import com.ivy.legacy.ui.modal.ModalTitle
import com.ivy.legacy.ui.modal.edit.AccountModal
import com.ivy.legacy.ui.modal.edit.AmountModal
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.legacy.ui.theme.dynamicContrast
import com.ivy.legacy.ui.theme.style
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

@Suppress("CyclomaticComplexMethod", "LongMethod")
@Composable
internal fun BoxWithConstraintsScope.LoanModal(
    modal: LoanModalData?,
    dateTime: Instant,
    onSetDate: () -> Unit,
    onSetTime: () -> Unit,
    onCreateLoan: (CreateLoanData) -> Unit,
    onEditLoan: (Loan, Boolean) -> Unit,
    accounts: List<LegacyAccount> = emptyList(),
    onCreateAccount: (CreateAccountData) -> Unit = {},
    onPerformCalculations: () -> Unit = {},
    dismiss: () -> Unit,
) {
    val loan = modal?.loan
    val timeConverter = LocalTimeConverter.current

    var nameTextFieldValue by remember(modal) {
        mutableStateOf(selectEndTextFieldValue(loan?.name))
    }
    var dateTime = modal?.loan?.dateTime ?: with(timeConverter) {
        dateTime.toLocalDateTime()
    }
    var type by remember(modal) {
        mutableStateOf(modal?.loan?.type ?: LoanType.BORROW)
    }
    var amount by remember(modal) {
        mutableStateOf(modal?.loan?.amount ?: 0.0)
    }
    var color by remember(modal) {
        mutableStateOf(loan?.color?.let { Color(it) } ?: Ivy)
    }
    var icon by remember(modal) {
        mutableStateOf(loan?.icon)
    }
    var noteTextFieldValue by remember(modal) {
        mutableStateOf(selectEndTextFieldValue(loan?.note))
    }
    var currencyCode by remember(modal) {
        mutableStateOf(modal?.baseCurrency ?: "")
    }

    val initialSelectedAccount = modal?.selectedAccountId?.let { accountId ->
        accounts.firstOrNull { it.id == accountId }
    }
    var selectedAcc by remember(modal) {
        mutableStateOf(initialSelectedAccount)
    }
    LaunchedEffect(modal?.id, initialSelectedAccount) {
        if (selectedAcc == null && initialSelectedAccount != null) {
            selectedAcc = initialSelectedAccount
        }
    }

    var createLoanTrans by remember(modal) {
        mutableStateOf(modal?.createLoanTransaction ?: false)
    }

    var accountChangeModal by remember { mutableStateOf(false) }
    var amountModalVisible by remember { mutableStateOf(false) }
    var currencyModalVisible by remember { mutableStateOf(false) }
    var chooseIconModalVisible by remember(modal) {
        mutableStateOf(false)
    }

    var accountModalData: AccountModalData? by remember { mutableStateOf(null) }

    IvyModal(
        id = modal?.id,
        visible = modal != null,
        dismiss = dismiss,
        shiftIfKeyboardShown = false,
        PrimaryAction = {
            LoanModalAddSave(
                isEdit = modal?.loan != null,
                // enabled = nameTextFieldValue.text.isNullOrBlank().not() && amount > 0 && ((createLoanTrans && selectedAcc != null) || !createLoanTrans)
                enabled = nameTextFieldValue.text.isNullOrBlank().not() && amount > 0 && selectedAcc != null
            ) {
                val modalBaseCurrency = modal?.baseCurrency.orEmpty()
                accountChangeModal =
                    loan != null && initialSelectedAccount != null && currencyCode != (
                        initialSelectedAccount.currency
                            ?: modalBaseCurrency
                        )

                if (!accountChangeModal) {
                    save(
                        loan = loan,
                        nameTextFieldValue = nameTextFieldValue,
                        dateTime = dateTime,
                        noteTextFieldValue = noteTextFieldValue,
                        type = type,
                        color = color,
                        icon = icon,
                        amount = amount,
                        selectedAccount = selectedAcc,
                        createLoanTransaction = createLoanTrans,

                        onCreateLoan = onCreateLoan,
                        onEditLoan = onEditLoan,
                        dismiss = dismiss
                    )
                }
            }
        }
    ) {
        onCompositionStart {
            if (modal?.autoOpenAmountModal == true) {
                amountModalVisible = true
            }
        }

        Spacer(Modifier.height(32.dp))

        ModalTitle(
            text = if (modal?.loan != null) stringResource(R.string.edit_loan) else stringResource(R.string.new_loan),
        )

        Spacer(Modifier.height(24.dp))

        LoanIconNameRow(
            hint = stringResource(R.string.loan_name),
            defaultIcon = R.drawable.ic_custom_loan_m,
            color = color,
            icon = icon,

            autoFocusKeyboard = modal?.autoFocusKeyboard ?: true,

            nameTextFieldValue = nameTextFieldValue,
            setNameTextFieldValue = { nameTextFieldValue = it },
            showChooseIconModal = {
                chooseIconModalVisible = true
            }
        )

        Spacer(Modifier.height(24.dp))

        LoanDateTimeRow(
            dateTime = dateTime,
            onEditDate = onSetDate,
            onEditTime = onSetTime
        )

        Spacer(Modifier.height(24.dp))

        LoanTypePicker(
            type = type,
            onTypeSelected = { type = it }
        )

        Spacer(Modifier.height(24.dp))

        LoanColorPicker(
            selectedColor = color,
            onColorSelected = { color = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = stringResource(R.string.note),
            style = LegacyTheme.typo.b2.style(
                color = LegacyTheme.colors.pureInverse,
                fontWeight = FontWeight.ExtraBold
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        LoanModalNameInput(
            hint = stringResource(R.string.description_text_field_hint),
            autoFocusKeyboard = false,
            textFieldValue = noteTextFieldValue,
            setTextFieldValue = {
                noteTextFieldValue = it
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = stringResource(R.string.associated_account),
            style = LegacyTheme.typo.b2.style(
                color = LegacyTheme.colors.pureInverse,
                fontWeight = FontWeight.ExtraBold
            )
        )

        Spacer(Modifier.height(16.dp))

        LoanAccountPickerRow(
            accounts = accounts,
            selectedAccount = selectedAcc,
            onSelectedAccountChanged = {
                selectedAcc = it
                currencyCode = it.currency ?: getDefaultFIATCurrency().currencyCode
            },
            onAddNewAccount = {
                accountModalData = AccountModalData(
                    account = null,
                    baseCurrency = selectedAcc?.currency ?: "USD",
                    balance = 0.0
                )
            },
            childrenTestTag = "amount_modal_account"
        )

        Spacer(Modifier.height(16.dp))

        LoanCheckboxWithText(
            modifier = Modifier
                .padding(start = 16.dp)
                .align(Alignment.Start),
            text = stringResource(R.string.create_main_transaction),
            checked = createLoanTrans
        ) {
            createLoanTrans = it
        }

        Spacer(modifier = Modifier.height(24.dp))

        ModalAmountSection(
            label = stringResource(R.string.enter_loan_amount_uppercase),
            currency = currencyCode,
            amount = amount,
            amountPaddingTop = 40.dp,
            amountPaddingBottom = 40.dp,
        ) {
            amountModalVisible = true
        }
    }

    val amountModalId = remember(modal, amount) {
        UUID.randomUUID()
    }
    AmountModal(
        id = amountModalId,
        visible = amountModalVisible,
        currency = currencyCode,
        initialAmount = amount,
        dismiss = { amountModalVisible = false }
    ) { newAmount ->
        amount = newAmount
    }

    CurrencyModal(
        title = stringResource(R.string.choose_currency),
        initialCurrency = IvyCurrency.fromCode(currencyCode),
        visible = currencyModalVisible,
        dismiss = { currencyModalVisible = false }
    ) {
        currencyCode = it
    }

    AccountModal(
        modal = accountModalData,
        onCreateAccount = onCreateAccount,
        onEditAccount = { _, _ -> },
        dismiss = {
            accountModalData = null
        }
    )

    ChooseIconModal(
        visible = chooseIconModalVisible,
        initialIcon = icon ?: "loan",
        color = color,
        dismiss = { chooseIconModalVisible = false }
    ) {
        icon = it
    }

    DeleteModal(
        visible = accountChangeModal,
        title = stringResource(R.string.confirm_account_change),
        description = stringResource(R.string.confirm_account_change_warning),
        buttonText = stringResource(R.string.confirm),
        iconStart = R.drawable.ic_agreed,
        dismiss = {
            selectedAcc = initialSelectedAccount ?: selectedAcc
            accountChangeModal = false
        }
    ) {
        onPerformCalculations()
        save(
            loan = loan,
            nameTextFieldValue = nameTextFieldValue,
            dateTime = dateTime,
            noteTextFieldValue = noteTextFieldValue,
            type = type,
            color = color,
            icon = icon,
            amount = amount,
            selectedAccount = selectedAcc,
            createLoanTransaction = createLoanTrans,

            onCreateLoan = onCreateLoan,
            onEditLoan = onEditLoan,
            dismiss = dismiss
        )
        accountChangeModal = false
    }
}

@Suppress("ParameterNaming")
@Composable
private fun ColumnScope.LoanTypePicker(
    type: LoanType,
    onTypeSelected: (LoanType) -> Unit
) {
    Text(
        modifier = Modifier.padding(horizontal = 32.dp),
        text = stringResource(R.string.loan_type),
        style = LegacyTheme.typo.b2.style(
            color = LegacyTheme.colors.pureInverse,
            fontWeight = FontWeight.ExtraBold
        )
    )

    Spacer(Modifier.height(16.dp))

    Row(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .background(LegacyTheme.colors.medium, LegacyTheme.shapes.r2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(8.dp))

        SelectorButton(
            selected = type == LoanType.BORROW,
            label = stringResource(R.string.borrow_money)
        ) {
            onTypeSelected(LoanType.BORROW)
        }

        Spacer(Modifier.width(8.dp))

        SelectorButton(
            selected = type == LoanType.LEND,
            label = stringResource(R.string.lend_money)
        ) {
            onTypeSelected(LoanType.LEND)
        }

        Spacer(Modifier.width(8.dp))
    }
}

@Suppress("MagicNumber")
private val loanColorPickerColors = listOf(
    Color(0xFF6B4DFF), Color(0xFFC34CFF), Color(0xFFFF4CFF),
    Color(0xFF4CC3FF), Color(0xFF45E6E6), Color(0xFF457BE6),
    Color(0xFF14CC9E), Color(0xFF45E67B), Color(0xFF96E645),
    Color(0xFFC7E62E), Color(0xFFFFEE33), Color(0xFFF29F30),
    Color(0xFFE67B45), Color(0xFFFFC34C), Color(0xFFFF4060),
    Color(0xFFE62E2E), Color(0xFFFF4CA6),

    Color(0xFFD5CCFF), Color(0xFFEECCFF), Color(0xFFFFBFFF),
    Color(0xFFB3E6FF), Color(0xFFB3FFFF), Color(0xFFCCDDFF),
    Color(0xFFAAF2E0), Color(0xFF99FFBB), Color(0xFFCCFF99),
    Color(0xFFEEFF99), Color(0xFFFFF799), Color(0xFFFFDEB3),
    Color(0xFFFFCCB3), Color(0xFFFFDC99), Color(0xFFFFCCD5),
    Color(0xFFFFB3B3), Color(0xFFFFCCE6),

    Color(0xFF352680), Color(0xFF622680), Color(0xFF802680),
    Color(0xFF266280), Color(0xFF227373), Color(0xFF223D73),
    Color(0xFF0A664F), Color(0xFF22733D), Color(0xFF66804D),
    Color(0xFF637317), Color(0xFF807719), Color(0xFF734B17),
    Color(0xFF66371F), Color(0xFF806226), Color(0xFF801919),
    Color(0xFF802030), Color(0xFF802653),
)

@Composable
private fun ColumnScope.LoanColorPicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    Text(
        modifier = Modifier.padding(horizontal = 32.dp),
        text = stringResource(R.string.choose_color),
        style = LegacyTheme.typo.b2.style(
            color = LegacyTheme.colors.pureInverse,
            fontWeight = FontWeight.ExtraBold
        )
    )

    Spacer(Modifier.height(16.dp))

    val listState = rememberLazyListState()
    LaunchedEffect(selectedColor) {
        val selectedColorIndex = loanColorPickerColors.indexOf(selectedColor)
        if (selectedColorIndex != -1) {
            listState.scrollToItem(selectedColorIndex)
        }
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        state = listState
    ) {
        item {
            Spacer(Modifier.width(24.dp))
        }

        items(loanColorPickerColors.size) { index ->
            LoanColorItem(
                color = loanColorPickerColors[index],
                selectedColor = selectedColor,
                onSelected = onColorSelected
            )
        }
    }
}

@Composable
private fun LoanColorItem(
    color: Color,
    selectedColor: Color,
    onSelected: (Color) -> Unit
) {
    val selected = color == selectedColor
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .size(48.dp)
            .background(color, CircleShape)
            .thenIf(selected) {
                border(width = 4.dp, color = color.dynamicContrast(), CircleShape)
            }
            .clickable {
                onSelected(color)
            }
            .testTag("color_item_${color.value}"),
        contentAlignment = Alignment.Center
    ) {
    }

    Spacer(Modifier.width(if (selected) 16.dp else 24.dp))
}

@Composable
private fun RowScope.SelectorButton(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    val rFull = LegacyTheme.shapes.rFull
    Text(
        modifier = Modifier
            .weight(1f)
            .clip(LegacyTheme.shapes.rFull)
            .clickable {
                onClick()
            }
            .padding(vertical = 8.dp)
            .thenIf(selected) {
                background(IvyGradients.Ivy.asHorizontalBrush(), rFull)
            }
            .padding(vertical = 8.dp),
        text = label,
        style = LegacyTheme.typo.b2.style(
            color = if (selected) White else LegacyTheme.colors.gray,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
    )
}

private fun save(
    loan: Loan?,
    nameTextFieldValue: TextFieldValue,
    dateTime: LocalDateTime,
    noteTextFieldValue: TextFieldValue,
    type: LoanType,
    color: Color,
    icon: String?,
    amount: Double,
    selectedAccount: LegacyAccount? = null,
    createLoanTransaction: Boolean = false,

    onCreateLoan: (CreateLoanData) -> Unit,
    onEditLoan: (Loan, Boolean) -> Unit,
    dismiss: () -> Unit
) {
    if (loan != null) {
        onEditLoan(
            loan.copy(
                name = nameTextFieldValue.text.trim(),
                dateTime = dateTime,
                note = NotBlankTrimmedString.from(noteTextFieldValue.text).getOrNull()?.value,
                type = type,
                amount = amount,
                color = color.toArgb(),
                icon = icon,
                accountId = selectedAccount?.id
            ),
            createLoanTransaction
        )
    } else {
        onCreateLoan(
            CreateLoanData(
                name = nameTextFieldValue.text.trim(),
                type = type,
                amount = amount,
                color = color.toArgb(),
                icon = icon,
                accountId = selectedAccount?.id,
                createLoanTransaction = createLoanTransaction,
                dateTime = dateTime,
                note = NotBlankTrimmedString.from(noteTextFieldValue.text).getOrNull()?.value
            )
        )
    }

    dismiss()
}
