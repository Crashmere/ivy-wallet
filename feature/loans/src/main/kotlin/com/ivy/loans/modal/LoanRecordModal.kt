package com.ivy.loans.modal

import com.ivy.loans.LoansTheme

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.data.model.LoanRecordType
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.ui.time.LocalTimeConverter
import com.ivy.ui.compose.thenIf
import com.ivy.data.model.LoanRecord
import com.ivy.data.model.currency.getDefaultFIATCurrency
import com.ivy.ui.compose.onCompositionStart
import com.ivy.ui.compose.selectEndTextFieldValue
import com.ivy.ui.R
import com.ivy.data.model.CreateAccountData
import com.ivy.data.model.CreateLoanRecordData
import com.ivy.data.model.EditLoanRecordData
import com.ivy.ui.icon.ItemIconSDefaultIcon
import com.ivy.ui.modal.DeleteModal
import com.ivy.ui.modal.IvyModal
import com.ivy.ui.modal.ModalAmountSection
import com.ivy.ui.modal.ModalTitle
import com.ivy.ui.modal.AccountModal
import com.ivy.ui.modal.AccountModalSaveData
import com.ivy.ui.modal.AmountModal
import com.ivy.loans.model.LoanAccount
import java.time.Instant
import java.util.UUID

@Suppress("CyclomaticComplexMethod", "LongMethod")
@SuppressLint("ComposeModifierMissing")
@Composable
internal fun BoxWithConstraintsScope.LoanRecordModal(
    visible: Boolean,
    loanRecord: LoanRecord?,
    baseCurrency: String,
    loanAccountCurrencyCode: String? = null,
    selectedAccountId: UUID? = null,
    createLoanRecordTransaction: Boolean = false,
    isLoanInterest: Boolean = false,
    dateTime: Instant,
    onSetDate: () -> Unit,
    onSetTime: () -> Unit,
    onCreate: (CreateLoanRecordData) -> Unit,
    onEdit: (EditLoanRecordData) -> Unit,
    onDelete: (LoanRecord) -> Unit,
    dismiss: () -> Unit,
    accounts: List<LoanAccount> = emptyList(),
    onCreateAccount: (CreateAccountData) -> Unit = {},
) {
    val initialRecord = loanRecord
    val modalId = remember(visible) {
        UUID.randomUUID()
    }

    var noteTextFieldValue by remember(visible, initialRecord) {
        mutableStateOf(selectEndTextFieldValue(initialRecord?.note))
    }
    var currencyCode by remember(visible, baseCurrency) {
        mutableStateOf(baseCurrency)
    }
    var amount by remember(visible, initialRecord) {
        mutableStateOf(initialRecord?.amount ?: 0.0)
    }
    val initialSelectedAccount = selectedAccountId?.let { accountId ->
        accounts.firstOrNull { it.id == accountId }
    }
    var selectedAcc by remember(visible, selectedAccountId) {
        mutableStateOf(initialSelectedAccount)
    }
    LaunchedEffect(modalId, initialSelectedAccount) {
        if (selectedAcc == null && initialSelectedAccount != null) {
            selectedAcc = initialSelectedAccount
        }
    }
    var createLoanRecordTrans by remember(visible, createLoanRecordTransaction) {
        mutableStateOf(createLoanRecordTransaction)
    }
    var loanInterest by remember(visible, isLoanInterest) {
        mutableStateOf(isLoanInterest)
    }
    var reCalculate by remember(visible) {
        mutableStateOf(false)
    }
    var reCalculateVisible by remember(visible, loanAccountCurrencyCode, baseCurrency) {
        mutableStateOf(loanAccountCurrencyCode != null && loanAccountCurrencyCode != baseCurrency)
    }
    var loanRecordType by remember(visible, initialRecord) {
        mutableStateOf(initialRecord?.loanRecordType ?: LoanRecordType.DECREASE)
    }

    var dateTime = initialRecord?.dateTime ?: dateTime
    var amountModalVisible by remember { mutableStateOf(false) }
    var deleteModalVisible by remember(visible) { mutableStateOf(false) }
    var accountModalVisible by remember { mutableStateOf(false) }
    var accountModalBaseCurrency by remember { mutableStateOf("USD") }
    var accountChangeConformationModal by remember { mutableStateOf(false) }

    IvyModal(
        id = modalId,
        visible = visible,
        dismiss = dismiss,
        shiftIfKeyboardShown = true,
        PrimaryAction = {
            LoanModalAddSave(
                isEdit = initialRecord != null,
                enabled = amount > 0 && selectedAcc != null
                // enabled = amount > 0 && ((createLoanRecordTrans && selectedAcc != null) || !createLoanRecordTrans)
            ) {
                accountChangeConformationModal =
                    initialRecord != null && initialSelectedAccount != null &&
                            baseCurrency != currencyCode && currencyCode != loanAccountCurrencyCode

                if (!accountChangeConformationModal) {
                    save(
                        loanRecord = initialRecord,
                        noteTextFieldValue = noteTextFieldValue,
                        amount = amount,
                        dateTime = dateTime,
                        loanRecordInterest = loanInterest,
                        selectedAccount = selectedAcc,
                        createLoanRecordTransaction = createLoanRecordTrans,
                        reCalculateAmount = reCalculate,
                        loanRecordType = loanRecordType,

                        onCreate = onCreate,
                        onEdit = onEdit,
                        dismiss = dismiss,
                    )
                }
            }
        }
    ) {
        onCompositionStart {
            if (loanRecord == null) {
                amountModalVisible = true
            }
        }

        Spacer(Modifier.height(32.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModalTitle(
                text = if (initialRecord != null) {
                    stringResource(R.string.edit_record)
                } else {
                    stringResource(
                        R.string.new_record
                    )
                }
            )

            if (initialRecord != null) {
                Spacer(Modifier.weight(1f))

                LoanModalDelete {
                    deleteModalVisible = true
                }

                Spacer(Modifier.width(24.dp))
            }
        }

        Spacer(Modifier.height(24.dp))

        LoanModalNameInput(
            hint = stringResource(R.string.note),
            autoFocusKeyboard = false,
            textFieldValue = noteTextFieldValue,
            setTextFieldValue = {
                noteTextFieldValue = it
            }
        )

        Spacer(Modifier.height(24.dp))

        val timeConverter = LocalTimeConverter.current
        LoanDateTimeRow(
            dateTime = with(timeConverter) { dateTime.toLocalDateTime() },
            onEditDate = onSetDate,
            onEditTime = onSetTime,
        )

        Spacer(Modifier.height(24.dp))

        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = stringResource(R.string.associated_account),
            style = LoansTheme.typo.b2.copy(
                color = LoansTheme.colors.pureInverse,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Start
            )
        )

        Spacer(Modifier.height(16.dp))

        LoanAccountPickerRow(
            accounts = accounts,
            selectedAccount = selectedAcc,
            onSelectedAccountChanged = {
                currencyCode = it.currency ?: getDefaultFIATCurrency().currencyCode

                reCalculateVisible =
                    initialRecord?.convertedAmount != null && selectedAcc != null && currencyCode == baseCurrency
                // Unchecks the Recalculate Option if Recalculate Checkbox is not visible
                reCalculate = !reCalculateVisible

                selectedAcc = it
            },
            onAddNewAccount = {
                accountModalBaseCurrency = selectedAcc?.currency ?: "USD"
                accountModalVisible = true
            },
            childrenTestTag = "amount_modal_account"
        )
        Spacer(Modifier.height(16.dp))

        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = stringResource(R.string.loan_record_type),
            style = LoansTheme.typo.b2.copy(
                color = LoansTheme.colors.pureInverse,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Start
            )
        )

        Spacer(Modifier.height(16.dp))

        LoanRecordTypeRow(selectedRecordType = loanRecordType, onLoanRecordTypeChanged = {
            if (it == LoanRecordType.INCREASE) loanInterest = false
            loanRecordType = it
        })

        Spacer(Modifier.height(16.dp))

        LoanCheckboxWithText(
            modifier = Modifier
                .padding(start = 16.dp)
                .align(Alignment.Start),
            text = stringResource(R.string.create_main_transaction),
            checked = createLoanRecordTrans
        ) {
            createLoanRecordTrans = it
        }

        AnimatedVisibility(visible = loanRecordType == LoanRecordType.DECREASE) {
            LoanCheckboxWithText(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .align(Alignment.Start),
                text = stringResource(R.string.mark_as_interest),
                checked = loanInterest
            ) {
                loanInterest = it
            }
        }

        if (reCalculateVisible) {
            LoanCheckboxWithText(
                modifier = Modifier
                    .padding(start = 16.dp, end = 8.dp)
                    .align(Alignment.Start),
                text = stringResource(R.string.recalculate_amount_with_today_exchange_rates),
                checked = reCalculate
            ) {
                reCalculate = it
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ModalAmountSection(
            label = stringResource(R.string.enter_record_amount_uppercase),
            currency = currencyCode,
            amount = amount,
            amountPaddingTop = 40.dp,
            amountPaddingBottom = 40.dp,
        ) {
            amountModalVisible = true
        }
    }

    val amountModalId = remember(visible, amount) {
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

    DeleteModal(
        visible = deleteModalVisible,
        title = stringResource(R.string.confirm_deletion),
        description = stringResource(R.string.record_deletion_warning, noteTextFieldValue.text),
        dismiss = { deleteModalVisible = false }
    ) {
        if (initialRecord != null) {
            onDelete(initialRecord)
        }
        deleteModalVisible = false
        reCalculate = false
        dismiss()
    }

    AccountModal(
        visible = accountModalVisible,
        account = null,
        baseCurrency = accountModalBaseCurrency,
        balance = 0.0,
        onCreateAccount = { onCreateAccount(it.toCreateAccountData()) },
        onEditAccount = { _, _ -> },
        dismiss = {
            accountModalVisible = false
        }
    )

    DeleteModal(
        visible = accountChangeConformationModal,
        title = stringResource(R.string.confirm_account_change),
        description = stringResource(R.string.account_change_warning),
        buttonText = stringResource(R.string.confirm),
        iconStart = R.drawable.ic_agreed,
        dismiss = {
            selectedAcc = initialSelectedAccount ?: selectedAcc
            accountChangeConformationModal = false
        }
    ) {
        save(
            loanRecord = initialRecord,
            noteTextFieldValue = noteTextFieldValue,
            amount = amount,
            dateTime = dateTime,
            loanRecordInterest = loanInterest,
            selectedAccount = selectedAcc,
            createLoanRecordTransaction = createLoanRecordTrans,
            reCalculateAmount = reCalculate,
            loanRecordType = loanRecordType,

            onCreate = onCreate,
            onEdit = onEdit,
            dismiss = dismiss,
        )

        accountChangeConformationModal = false
    }
}

private fun AccountModalSaveData.toCreateAccountData() = CreateAccountData(
    name = name,
    currency = currency,
    color = color,
    icon = icon,
    balance = balance,
    includeBalance = includeInBalance,
)

private fun save(
    loanRecord: LoanRecord?,
    noteTextFieldValue: TextFieldValue,
    amount: Double,
    dateTime: Instant,
    loanRecordInterest: Boolean = false,
    createLoanRecordTransaction: Boolean = false,
    selectedAccount: LoanAccount? = null,
    reCalculateAmount: Boolean = false,
    loanRecordType: LoanRecordType,

    onCreate: (CreateLoanRecordData) -> Unit,
    onEdit: (EditLoanRecordData) -> Unit,
    dismiss: () -> Unit
) {
    if (loanRecord != null) {
        val record = loanRecord.copy(
            note = NotBlankTrimmedString.from(noteTextFieldValue.text).getOrNull()?.value,
            amount = amount,
            dateTime = dateTime,
            interest = loanRecordInterest,
            accountId = selectedAccount?.id,
            loanRecordType = loanRecordType
        )
        onEdit(
            EditLoanRecordData(
                newLoanRecord = record,
                originalLoanRecord = loanRecord,
                createLoanRecordTransaction = createLoanRecordTransaction,
                reCalculateLoanAmount = reCalculateAmount,
            )
        )
    } else {
        onCreate(
            CreateLoanRecordData(
                note = NotBlankTrimmedString.from(noteTextFieldValue.text).getOrNull()?.value,
                amount = amount,
                dateTime = dateTime,
                interest = loanRecordInterest,
                accountId = selectedAccount?.id,
                createLoanRecordTransaction = createLoanRecordTransaction,
                loanRecordType = loanRecordType
            )
        )
    }

    dismiss()
}

@Composable
@Suppress("ParameterNaming")
private fun LoanRecordTypeRow(
    selectedRecordType: LoanRecordType?,
    modifier: Modifier = Modifier,
    onLoanRecordTypeChanged: (LoanRecordType) -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.width(24.dp))
        LoanRecordType(
            modifier = Modifier,
            loanRecordType = LoanRecordType.DECREASE,
            selectedRecordType = selectedRecordType
        ) {
            onLoanRecordTypeChanged(it)
        }
        Spacer(modifier = Modifier.width(8.dp))
        LoanRecordType(
            modifier = Modifier,
            loanRecordType = LoanRecordType.INCREASE,
            selectedRecordType = selectedRecordType
        ) {
            onLoanRecordTypeChanged(it)
        }
    }
}

@Composable
private fun LoanRecordType(
    loanRecordType: LoanRecordType,
    selectedRecordType: LoanRecordType?,
    modifier: Modifier = Modifier,
    onClick: (LoanRecordType) -> Unit
) {
    val (text, iconDrawable) =
        if (loanRecordType == LoanRecordType.INCREASE) {
            stringResource(id = R.string.increase_loan) to R.drawable.ic_donate_plus
        } else {
            stringResource(id = R.string.decrease_loan) to R.drawable.ic_donate_minus
        }
    val selected = selectedRecordType == loanRecordType
    val medium = LoansTheme.colors.medium
    val rFull = LoansTheme.shapes.rFull
    val selectedColor = LoansTheme.colors.green1
    Row(
        modifier = modifier
            .clip(LoansTheme.shapes.rFull)
            .thenIf(!selected) {
                border(2.dp, medium, rFull)
            }
            .thenIf(selected) {
                background(selectedColor, rFull)
            }
            .clickable(onClick = { onClick(loanRecordType) }),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(12.dp))

        ItemIconSDefaultIcon(
            defaultIcon = iconDrawable,
            iconName = null,
            tint = LoansTheme.colors.pureInverse
        )

        Spacer(Modifier.width(4.dp))

        Text(
            modifier = Modifier.padding(vertical = 10.dp),
            text = text,
            style = LoansTheme.typo.b2.copy(
                color = LoansTheme.colors.pureInverse,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Start
            )
        )
        Spacer(Modifier.width(24.dp))
    }
}

