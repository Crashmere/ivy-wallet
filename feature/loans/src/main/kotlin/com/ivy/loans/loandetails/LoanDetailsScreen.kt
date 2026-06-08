package com.ivy.loans.loandetails

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.ui.navigation.screenScopedViewModel
import com.ivy.data.model.TransactionType
import com.ivy.data.model.processByType
import com.ivy.data.model.LoanType
import com.ivy.ui.time.LocalTimeFormatter
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.legacy.ui.theme.style
import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.data.model.Loan
import com.ivy.data.model.LoanRecord
import com.ivy.loans.humanReadableType
import com.ivy.ui.compose.clickableNoIndication
import com.ivy.ui.compose.drawColoredShadow
import com.ivy.data.model.currency.format
import com.ivy.ui.time.formatNicely
import com.ivy.ui.compose.rememberInteractionSource
import com.ivy.ui.platform.setStatusBarDarkTextCompat
import com.ivy.loans.model.DisplayLoanAccount
import com.ivy.loans.model.DisplayLoanRecord
import com.ivy.loans.loandetails.events.DeleteLoanModalEvent
import com.ivy.loans.loandetails.events.LoanDetailsScreenEvent
import com.ivy.loans.loandetails.events.LoanModalEvent
import com.ivy.loans.loandetails.events.LoanRecordModalEvent
import com.ivy.ui.navigation.LoanDetailsScreen
import com.ivy.ui.navigation.TransactionsScreen
import com.ivy.ui.navigation.navigation
import com.ivy.ui.R
import com.ivy.ui.time.TimeFormatter
import com.ivy.data.model.currency.IvyCurrency
import com.ivy.legacy.ui.theme.Gradient
import com.ivy.legacy.ui.theme.GradientGreen
import com.ivy.legacy.ui.theme.GradientIvy
import com.ivy.legacy.ui.theme.Gray
import com.ivy.legacy.ui.theme.Green
import com.ivy.legacy.ui.theme.Ivy
import com.ivy.legacy.ui.money.AmountCurrencyB1
import com.ivy.legacy.ui.money.BalanceRow
import com.ivy.legacy.ui.icon.ItemIconMDefaultIcon
import com.ivy.legacy.ui.button.IvyButton
import com.ivy.legacy.ui.button.IvyCircleButton
import com.ivy.legacy.ui.icon.IvyIcon
import com.ivy.legacy.ui.button.IvyOutlinedButton
import com.ivy.ui.icon.getCustomIconIdS
import com.ivy.legacy.ui.theme.Red
import com.ivy.legacy.ui.theme.White
import com.ivy.legacy.ui.theme.dynamicContrast
import com.ivy.legacy.ui.theme.findContrastTextColor
import com.ivy.legacy.ui.theme.isDarkColor
import com.ivy.legacy.ui.modal.DeleteModal
import com.ivy.loans.modal.LoanModal
import com.ivy.loans.modal.LoanRecordModal
import com.ivy.legacy.ui.modal.ProgressModal
import com.ivy.legacy.ui.theme.toComposeColor
import com.ivy.loans.LoanProgressBar
import java.util.UUID

private val LoanCardDarkBackground = Color(0xFF2B2C2D)
private val LoanCardLightBackground = Color(0xFFEFEEF0)

@Composable
fun BoxWithConstraintsScope.LoanDetailsScreen(screen: LoanDetailsScreen) {
    val viewModel: LoanDetailsViewModel = screenScopedViewModel()
    val nav = navigation()
    val state = viewModel.uiState()

    LaunchedEffect(viewModel, screen.loanId) {
        viewModel.start(screen.loanId)
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                LoanDetailsUiEvent.CloseScreen -> nav.back()
            }
        }
    }

    UI(
        state = state,
        onEventHandler = viewModel::onEvent,
        onClose = nav::back,
        onAccountClick = { accountId ->
            nav.navigateTo(
                TransactionsScreen(
                    accountId = accountId,
                    categoryId = null
                )
            )
        }
    )
}

@Composable
private fun BoxWithConstraintsScope.UI(
    state: LoanDetailsScreenState,
    onEventHandler: (LoanDetailsScreenEvent) -> Unit = {},
    onClose: () -> Unit,
    onAccountClick: (UUID) -> Unit
) {
    val itemColor = state.loan?.color?.toComposeColor() ?: Gray
    val selectedLoanAccount = state.accounts.firstOrNull { account ->
        account.id == state.selectedLoanAccountId
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(itemColor)
    ) {
        val listState = rememberLazyListState()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 16.dp)
                .clip(LegacyTheme.shapes.r1Top)
                .background(LegacyTheme.colors.pure),
            state = listState,
        ) {
            item {
                if (state.loan != null) {
                    Header(
                        loan = state.loan,
                        baseCurrency = state.baseCurrency,
                        loanTotalAmount = state.loanTotalAmount,
                        amountPaid = state.amountPaid,
                        loanAmountPaid = state.loanAmountPaid,
                        itemColor = itemColor,
                        selectedLoanAccount = selectedLoanAccount,
                        onAmountClick = {
                            onEventHandler.invoke(LoanDetailsScreenEvent.OnAmountClick)
                        },
                        onDeleteLoan = {
                            onEventHandler.invoke(
                                DeleteLoanModalEvent.OnDismissDeleteLoan(
                                    isDeleteModalVisible = true
                                )
                            )
                        },
                        onEditLoan = {
                            onEventHandler.invoke(LoanDetailsScreenEvent.OnEditLoanClick)
                        },
                        onAddRecord = {
                            onEventHandler.invoke(LoanDetailsScreenEvent.OnAddRecord)
                        },
                        onClose = onClose,
                        onAccountClick = onAccountClick
                    )
                }
            }

            item {
                // Rounded corners top effect
                Spacer(
                    Modifier
                        .height(32.dp)
                        .fillMaxWidth()
                        .background(itemColor) // itemColor is displayed below the clip
                        .background(LegacyTheme.colors.pure, LegacyTheme.shapes.r1Top)
                )
            }

            if (state.loan != null) {
                loanRecords(
                    loan = state.loan,
                    displayLoanRecords = state.displayLoanRecords,
                    onClick = { loanRecordId ->
                        onEventHandler.invoke(
                            LoanRecordModalEvent.OnClickLoanRecord(
                                loanRecordId
                            )
                        )
                    },
                    onAccountClick = onAccountClick
                )
                item {
                    InitialRecordItem(
                        loan = state.loan,
                        amount = state.loan.amount,
                        baseCurrency = state.baseCurrency,
                    )
                }
            }

            if (state.displayLoanRecords.isEmpty()) {
                item {
                    NoLoanRecordsEmptyState()
                    Spacer(Modifier.height(96.dp))
                }
            }

            item {
                // scroll hack
                Spacer(Modifier.height(96.dp))
            }
        }
    }

    LoanModal(
        modal = state.loanModalData, onCreateLoan = {
        // do nothing
    }, onEditLoan = { loan, createLoanTransaction ->
        onEventHandler.invoke(LoanModalEvent.OnEditLoanModal(loan, createLoanTransaction))
    }, dismiss = {
        onEventHandler.invoke(LoanModalEvent.OnDismissLoanModal)
    }, onCreateAccount = { createAccountData ->
        onEventHandler.invoke(LoanDetailsScreenEvent.OnCreateAccount(createAccountData))
    }, accounts = state.accounts, onPerformCalculations = {
        onEventHandler.invoke(LoanModalEvent.PerformCalculation)
    }, dateTime = state.dateTime,
        onSetDate = {
            onEventHandler.invoke(LoanModalEvent.OnChangeDate)
        },
        onSetTime = {
            onEventHandler.invoke(LoanModalEvent.OnChangeTime)
        },
    )

    LoanRecordModal(
        modal = state.loanRecordModalData, onCreate = {
        onEventHandler.invoke(LoanRecordModalEvent.OnCreateLoanRecord(it))
    }, onEdit = {
        onEventHandler.invoke(LoanRecordModalEvent.OnEditLoanRecord(it))
    }, onDelete = { loanRecord ->
        onEventHandler.invoke(LoanRecordModalEvent.OnDeleteLoanRecord(loanRecord.id))
    }, accounts = state.accounts, dismiss = {
        onEventHandler.invoke(LoanRecordModalEvent.OnDismissLoanRecord)
    }, onCreateAccount = { createAccountData ->
        onEventHandler.invoke(LoanDetailsScreenEvent.OnCreateAccount(createAccountData))
    },
        dateTime = state.dateTime,
        onSetDate = {
            onEventHandler.invoke(LoanRecordModalEvent.OnChangeDate)
        },
        onSetTime = {
            onEventHandler.invoke(LoanRecordModalEvent.OnChangeTime)
        },
    )

    DeleteModal(
        visible = state.isDeleteModalVisible,
        title = stringResource(R.string.confirm_deletion),
        description = stringResource(R.string.loan_confirm_deletion_description),
        dismiss = {
            onEventHandler.invoke(DeleteLoanModalEvent.OnDismissDeleteLoan(isDeleteModalVisible = false))
        }
    ) {
        onEventHandler.invoke(DeleteLoanModalEvent.OnDeleteLoan)
    }

    ProgressModal(
        title = stringResource(R.string.confirm_account_change),
        description = stringResource(R.string.confirm_account_loan_change),
        visible = state.waitModalVisible
    )
}

@Composable
private fun Header(
    loan: Loan,
    baseCurrency: String,
    loanTotalAmount: Double,
    amountPaid: Double,
    itemColor: Color,
    onAmountClick: () -> Unit,
    onEditLoan: () -> Unit,
    onDeleteLoan: () -> Unit,
    loanAmountPaid: Double = 0.0,
    selectedLoanAccount: LegacyAccount? = null,
    onAddRecord: () -> Unit,
    onClose: () -> Unit,
    onAccountClick: (UUID) -> Unit
) {
    val contrastColor = findContrastTextColor(itemColor)

    val darkColor = isDarkColor(itemColor)
    setStatusBarDarkTextCompat(darkText = !darkColor)

    Column(
        modifier = Modifier.background(itemColor)
    ) {
        Spacer(Modifier.height(20.dp))

        LoanStatisticToolbar(
            contrastColor = contrastColor,
            onClose = onClose,
            onEdit = onEditLoan,
            onDelete = onDeleteLoan
        )

        Spacer(Modifier.height(24.dp))

        LoanItem(
            loan = loan,
            contrastColor = contrastColor,
        ) {
            onEditLoan()
        }

        BalanceRow(
            modifier = Modifier
                .padding(start = 32.dp)
                .testTag("loan_amount")
                .clickableNoIndication(rememberInteractionSource()) {
                    onAmountClick()
                },
            textColor = contrastColor,
            currency = baseCurrency,
            balance = loanTotalAmount,
        )

        Spacer(Modifier.height(20.dp))

        LoanInfoCard(
            loan = loan,
            baseCurrency = baseCurrency,
            amountPaid = amountPaid,
            loanAmountPaid = loanAmountPaid,
            loanTotalAmount = loanTotalAmount,
            selectedLoanAccount = selectedLoanAccount,
            onAddRecord = onAddRecord,
            onAccountClick = onAccountClick
        )

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun LoanStatisticToolbar(
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

        LoanStatisticToolbarCloseButton(
            contrastColor = contrastColor,
            onClose = onClose
        )

        Spacer(Modifier.weight(1f))

        if (showEditButton) {
            IvyOutlinedButton(
                iconStart = R.drawable.ic_edit,
                text = stringResource(R.string.edit),
                borderColor = contrastColor,
                iconTint = contrastColor,
                textColor = contrastColor,
                solidBackground = false
            ) {
                onEdit()
            }
        }

        Spacer(Modifier.width(16.dp))

        if (showDeleteButton) {
            LoanStatisticToolbarDeleteButton {
                onDelete()
            }
        }

        Spacer(Modifier.width(24.dp))
    }
}

@Composable
private fun LoanStatisticToolbarCloseButton(
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
private fun LoanStatisticToolbarDeleteButton(
    onDelete: () -> Unit
) {
    IvyCircleButton(
        modifier = Modifier
            .size(48.dp)
            .testTag("delete_button"),
        backgroundPadding = 6.dp,
        icon = R.drawable.ic_delete,
        backgroundGradient = Gradient.solid(Red),
        enabled = true,
        tint = White,
        onClick = onDelete
    )
}

@Composable
private fun LoanItem(
    loan: Loan,
    contrastColor: Color,

    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(start = 22.dp)
            .clickableNoIndication(rememberInteractionSource()) {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        ItemIconMDefaultIcon(
            iconName = loan.icon,
            defaultIcon = R.drawable.ic_custom_loan_m,
            tint = contrastColor
        )

        Spacer(Modifier.width(8.dp))

        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.testTag("loan_name"),
                    text = loan.name,
                    style = LegacyTheme.typo.b1.style(
                        color = contrastColor,
                        fontWeight = FontWeight.ExtraBold
                    )
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = loan.humanReadableType(),
                    style = LegacyTheme.typo.c.style(
                        color = loan.color.toComposeColor().dynamicContrast()
                    )
                )
            }

            loan.dateTime?.let {
                Text(
                    text = it.formatNicely(
                        noWeekDay = false
                    ).uppercase(),
                    style = LegacyTheme.typo.nC.style(
                        color = contrastColor
                    )
                )
            }
        }
    }
}

@Suppress("LongMethod")
@Composable
private fun LoanInfoCard(
    loan: Loan,
    baseCurrency: String,
    loanTotalAmount: Double,
    amountPaid: Double,
    loanAmountPaid: Double = 0.0,
    selectedLoanAccount: LegacyAccount? = null,

    onAddRecord: () -> Unit,
    onAccountClick: (UUID) -> Unit
) {
    val backgroundColor = if (isDarkColor(loan.color.toComposeColor())) {
        LoanCardDarkBackground.copy(alpha = 0.9f)
    } else {
        LoanCardLightBackground.copy(alpha = 0.9f)
    }

    val contrastColor = findContrastTextColor(backgroundColor)
    val percentPaid = amountPaid / loanTotalAmount
    val loanPercentPaid = loanAmountPaid / loanTotalAmount
    val leftToPay = loanTotalAmount - amountPaid

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .drawColoredShadow(
                color = backgroundColor,
                alpha = 0.1f
            )
            .background(backgroundColor, LegacyTheme.shapes.r2),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                modifier = Modifier.padding(top = 8.dp, start = 24.dp),
                text = stringResource(R.string.paid),
                style = LegacyTheme.typo.c.style(
                    color = contrastColor,
                    fontWeight = FontWeight.ExtraBold
                )
            )
            if (selectedLoanAccount != null) {
                IvyButton(
                    modifier = Modifier.padding(end = 16.dp, top = 12.dp),
                    backgroundGradient = Gradient.solid(loan.color.toComposeColor()),
                    hasGlow = false,
                    iconTint = contrastColor,
                    text = selectedLoanAccount.name,
                    iconStart = getCustomIconIdS(
                        iconName = selectedLoanAccount.icon,
                        defaultIcon = R.drawable.ic_custom_account_s
                    ),
                    textStyle = LegacyTheme.typo.c.style(
                        color = contrastColor,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    padding = 8.dp,
                    iconEdgePadding = 10.dp
                ) {
                    onAccountClick(selectedLoanAccount.id)
                }
            }
        }

        // Support UI for Old Versions where
        if (selectedLoanAccount == null) {
            Spacer(Modifier.height(12.dp))
        }

        Text(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .testTag("amount_paid"),
            text = "${amountPaid.format(baseCurrency)} / ${loanTotalAmount.format(baseCurrency)}",
            style = LegacyTheme.typo.nB1.style(
                color = contrastColor,
                fontWeight = FontWeight.ExtraBold
            )
        )
        Text(
            modifier = Modifier.padding(horizontal = 24.dp),
            text = IvyCurrency.fromCode(baseCurrency)?.name ?: "",
            style = LegacyTheme.typo.b2.style(
                color = contrastColor,
                fontWeight = FontWeight.Normal
            )
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.testTag("percent_paid"),
                text = "${percentPaid.times(100).format(2)}%",
                style = LegacyTheme.typo.nB1.style(
                    color = contrastColor,
                    fontWeight = FontWeight.ExtraBold
                )
            )

            Spacer(Modifier.width(8.dp))

            Text(
                modifier = Modifier.testTag("left_to_pay"),
                text = stringResource(
                    R.string.left_to_pay,
                    leftToPay.format(baseCurrency),
                    baseCurrency
                ),
                style = LegacyTheme.typo.nB2.style(
                    color = Gray,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }

        Spacer(Modifier.height(8.dp))

        LoanProgressBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .padding(horizontal = 24.dp),
            notFilledColor = LegacyTheme.colors.pure,
            percent = percentPaid
        )

        if (loanAmountPaid != 0.0) {
            HorizontalDivider(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .fillMaxWidth(),
                thickness = 1.dp,
                color = contrastColor
            )

            Text(
                modifier = Modifier.padding(horizontal = 24.dp),
                text = stringResource(R.string.loan_interest),
                style = LegacyTheme.typo.c.style(
                    color = contrastColor,
                    fontWeight = FontWeight.ExtraBold
                )
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.testTag("loan_interest_percent_paid"),
                    text = "${loanPercentPaid.times(100).format(2)}%",
                    style = LegacyTheme.typo.nB1.style(
                        color = contrastColor,
                        fontWeight = FontWeight.ExtraBold
                    )
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    modifier = Modifier.testTag("interest_paid"),
                    text = stringResource(
                        R.string.interest_paid,
                        loanAmountPaid.format(baseCurrency),
                        baseCurrency
                    ),
                    style = LegacyTheme.typo.nB2.style(
                        color = Gray,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            LoanProgressBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .padding(horizontal = 24.dp),
                notFilledColor = LegacyTheme.colors.pure,
                percent = loanPercentPaid
            )
        }

        Spacer(Modifier.height(24.dp))

        IvyButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .align(Alignment.CenterHorizontally),
            text = stringResource(R.string.add_record),
            shadowAlpha = 0.1f,
            backgroundGradient = Gradient.solid(contrastColor),
            textStyle = LegacyTheme.typo.b2.style(
                color = findContrastTextColor(contrastColor),
                fontWeight = FontWeight.Bold
            ),
            wrapContentMode = false
        ) {
            onAddRecord()
        }

        Spacer(Modifier.height(12.dp))
    }
}

internal fun LazyListScope.loanRecords(
    loan: Loan,
    displayLoanRecords: List<DisplayLoanRecord> = emptyList(),
    onClick: (UUID) -> Unit,
    onAccountClick: (UUID) -> Unit
) {
    items(items = displayLoanRecords) { displayLoanRecord ->
        LoanRecordItem(
            loan = loan,
            loanRecord = displayLoanRecord.loanRecord,
            baseCurrency = displayLoanRecord.loanRecordCurrencyCode,
            account = displayLoanRecord.account,
            loanBaseCurrency = displayLoanRecord.loanCurrencyCode,
            onAccountClick = onAccountClick
        ) {
            onClick(displayLoanRecord.loanRecord.id)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun LoanRecordItem(
    loan: Loan,
    loanRecord: LoanRecord,
    baseCurrency: String,
    loanBaseCurrency: String = "",
    account: DisplayLoanAccount? = null,
    onAccountClick: (UUID) -> Unit,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(LegacyTheme.shapes.r4)
            .clickable {
                onClick()
            }
            .background(LegacyTheme.colors.medium, LegacyTheme.shapes.r4)
            .testTag("loan_record_item")
    ) {
        if (account != null || loanRecord.interest) {
            Row(Modifier.padding(16.dp)) {
                if (account != null) {
                    IvyButton(
                        backgroundGradient = Gradient.solid(LegacyTheme.colors.pure),
                        hasGlow = false,
                        iconTint = LegacyTheme.colors.pureInverse,
                        text = account.name,
                        iconStart = getCustomIconIdS(
                            iconName = account.icon,
                            defaultIcon = R.drawable.ic_custom_account_s
                        ),
                        textStyle = LegacyTheme.typo.c.style(
                            color = LegacyTheme.colors.pureInverse,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        padding = 8.dp,
                        iconEdgePadding = 10.dp
                    ) {
                        onAccountClick(account.id)
                    }
                }

                if (loanRecord.interest) {
                    // Spacer(modifier = Modifier.width(8.dp))

                    val textIconColor = if (isDarkColor(loan.color.toComposeColor())) {
                        LoanCardLightBackground
                    } else {
                        LoanCardDarkBackground
                    }

                    IvyButton(
                        modifier = Modifier.padding(start = 8.dp),
                        backgroundGradient = Gradient.solid(loan.color.toComposeColor()),
                        hasGlow = false,
                        iconTint = textIconColor,
                        text = stringResource(R.string.interest),
                        iconStart = getCustomIconIdS(
                            iconName = "currency",
                            defaultIcon = R.drawable.ic_currency
                        ),
                        textStyle = LegacyTheme.typo.c.style(
                            color = textIconColor,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        padding = 8.dp,
                        iconEdgePadding = 10.dp
                    ) {
                        // do Nothing
                    }
                }
            }
        } else {
            Spacer(Modifier.height(20.dp))
        }

        val timeFormatter = LocalTimeFormatter.current
        Text(
            modifier = Modifier.padding(horizontal = 24.dp),
            text = with(timeFormatter) {
                loanRecord.dateTime.formatLocal(
                    TimeFormatter.Style.DateAndTime(includeWeekDay = true)
                ).uppercase()
            },
            style = LegacyTheme.typo.nC.style(
                color = Gray,
                fontWeight = FontWeight.Bold
            )
        )

        if (loanRecord.note.isNullOrBlank().not()) {
            Text(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                text = loanRecord.note!!,
                style = LegacyTheme.typo.b1.style(
                    fontWeight = FontWeight.ExtraBold,
                    color = LegacyTheme.colors.pureInverse
                )
            )
        }

        if (loanRecord.note.isNullOrEmpty()) {
            Spacer(Modifier.height(16.dp))
        }
        val transactionType = when (loan.type) {
            LoanType.LEND -> {
                loanRecord.loanRecordType.processByType(
                    increaseAction = { TransactionType.EXPENSE },
                    decreaseAction = { TransactionType.INCOME }
                )
            }

            LoanType.BORROW -> {
                loanRecord.loanRecordType.processByType(
                    increaseAction = { TransactionType.INCOME },
                    decreaseAction = { TransactionType.EXPENSE }
                )
            }
        }
        LoanRecordAmountRow(
            transactionType = transactionType,
            currency = baseCurrency,
            amount = loanRecord.amount
        )

        if (loanRecord.convertedAmount != null) {
            Text(
                modifier = Modifier.padding(start = 68.dp),
                text = loanRecord.convertedAmount!!.format(baseCurrency) + " $loanBaseCurrency",
                style = LegacyTheme.typo.nB2.style(
                    color = Gray,
                    fontWeight = FontWeight.Normal
                )
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun InitialRecordItem(
    loan: Loan,
    amount: Double,
    baseCurrency: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(LegacyTheme.shapes.r4)
            .background(LegacyTheme.colors.medium, LegacyTheme.shapes.r4)
            .testTag("loan_record_item")
    ) {
        IvyButton(
            modifier = Modifier.padding(16.dp),
            backgroundGradient = Gradient.solid(LegacyTheme.colors.pure),
            text = stringResource(id = R.string.initial_loan_record),
            iconTint = LegacyTheme.colors.pureInverse,
            iconStart = getCustomIconIdS(
                iconName = loan.icon,
                defaultIcon = R.drawable.ic_custom_loan_s
            ),
            textStyle = LegacyTheme.typo.c.style(
                color = LegacyTheme.colors.pureInverse,
                fontWeight = FontWeight.ExtraBold
            ),
            padding = 8.dp,
        ) {}

        val timeFormatter = LocalTimeFormatter.current

        loan.dateTime?.let { dateTime ->
            Text(
                modifier = Modifier.padding(horizontal = 24.dp),
                text = with(timeFormatter) {
                    dateTime.format(
                        TimeFormatter.Style.DateAndTime(includeWeekDay = true)
                    ).uppercase()
                },
                style = LegacyTheme.typo.nC.style(
                    color = Gray,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        if (loan.note.isNullOrBlank().not()) {
            Text(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                text = loan.note!!,
                style = LegacyTheme.typo.b1.style(
                    fontWeight = FontWeight.ExtraBold,
                    color = LegacyTheme.colors.pureInverse
                )
            )
        }

        if (loan.note.isNullOrEmpty()) {
            Spacer(Modifier.height(16.dp))
        }

        LoanRecordAmountRow(
            transactionType = if (loan.type == LoanType.LEND) TransactionType.EXPENSE else TransactionType.INCOME,
            currency = baseCurrency,
            amount = amount
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun LoanRecordAmountRow(
    transactionType: TransactionType,
    currency: String,
    amount: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.testTag("type_amount_currency"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(24.dp))

        val style = when (transactionType) {
            TransactionType.INCOME -> LoanRecordAmountTypeStyle(
                icon = R.drawable.ic_income,
                gradient = GradientGreen,
                iconTint = White,
                textColor = Green
            )

            TransactionType.EXPENSE -> LoanRecordAmountTypeStyle(
                icon = R.drawable.ic_expense,
                gradient = Gradient(LegacyTheme.colors.gray, LegacyTheme.colors.pureInverse),
                iconTint = White,
                textColor = LegacyTheme.colors.pureInverse
            )

            TransactionType.TRANSFER -> LoanRecordAmountTypeStyle(
                icon = R.drawable.ic_transfer,
                gradient = GradientIvy,
                iconTint = White,
                textColor = Ivy
            )
        }

        IvyIcon(
            modifier = Modifier
                .background(style.gradient.asHorizontalBrush(), CircleShape),
            icon = style.icon,
            tint = style.iconTint
        )

        Spacer(Modifier.width(12.dp))

        AmountCurrencyB1(
            amount = amount,
            currency = currency,
            textColor = style.textColor
        )

        Spacer(Modifier.width(24.dp))
    }
}

private data class LoanRecordAmountTypeStyle(
    @DrawableRes val icon: Int,
    val gradient: Gradient,
    val iconTint: Color,
    val textColor: Color
)

@Composable
private fun NoLoanRecordsEmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))

        IvyIcon(
            icon = R.drawable.ic_notransactions,
            tint = Gray
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.no_records),
            style = LegacyTheme.typo.b1.style(
                color = Gray,
                fontWeight = FontWeight.ExtraBold
            )
        )

        Spacer(Modifier.height(8.dp))

        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = stringResource(R.string.no_records_for_the_loan),
            style = LegacyTheme.typo.b2.style(
                color = Gray,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        )
    }
}
