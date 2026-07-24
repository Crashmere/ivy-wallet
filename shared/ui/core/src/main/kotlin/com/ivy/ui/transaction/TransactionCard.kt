package com.ivy.ui.transaction

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ivy.ui.R
import com.ivy.ui.compose.GradientButton
import com.ivy.ui.icon.ItemIconSDefaultIcon
import com.ivy.ui.money.formatAmount
import com.ivy.ui.theme.colors.Gradient
import com.ivy.ui.theme.colors.findContrastTextColor
import com.ivy.ui.theme.colors.toComposeColor
import com.ivy.ui.theme.colors.IvyFixedColors.Gray
import com.ivy.ui.theme.colors.IvyFixedColors.Green
import com.ivy.ui.theme.colors.IvyFixedColors.Ivy
import com.ivy.ui.theme.colors.IvyFixedColors.Orange
import com.ivy.ui.theme.colors.IvyFixedColors.Red
import com.ivy.ui.theme.colors.IvyFixedColors.White
import com.ivy.ui.theme.colors.IvyGradients.Green as GradientGreen
import com.ivy.ui.time.LocalTimeConverter
import com.ivy.ui.time.LocalTimeFormatter
import com.ivy.ui.time.LocalTimeProvider
import com.ivy.ui.time.TimeFormatter
import java.util.UUID

@Suppress("CyclomaticComplexMethod", "LongMethod", "UnusedParameter")
@Composable
internal fun TransactionCard(
    baseData: TransactionListData,
    transaction: TransactionListTransaction,
    tags: List<TransactionListTag> = emptyList(),
    shouldShowAccountSpecificColorInTransactions: Boolean,
    onPayOrGet: (UUID) -> Unit,
    modifier: Modifier = Modifier,
    onSkipTransaction: (UUID) -> Unit = {},
    onAccountClick: (UUID) -> Unit,
    onCategoryClick: (UUID) -> Unit,
    onClick: (UUID, TransactionListTransactionType) -> Unit,
) {
    val card = transaction
    val sourceAccount = remember(baseData.accounts, card.accountId) {
        baseData.accounts.find { it.id == card.accountId }
    }
    val targetAccount = remember(baseData.accounts, card.toAccountId) {
        baseData.accounts.find { it.id == card.toAccountId }
    }
    val category = remember(baseData.categories, card.categoryId) {
        card.categoryId?.let { id -> baseData.categories.find { it.id == id } }
    }

    val transactionCurrency = sourceAccount?.currency ?: baseData.baseCurrency
    val toAccountCurrency = targetAccount?.currency ?: baseData.baseCurrency

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                if (sourceAccount != null) {
                    onClick(card.id, card.type)
                }
            }
            .testTag("transaction_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TransactionIcon(
                type = card.type,
                categoryColor = category?.color,
                categoryIcon = category?.icon,
                accountColor = sourceAccount?.color,
                accountIcon = sourceAccount?.icon,
            )

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = primaryText(card, category, sourceAccount),
                    style = TransactionListTheme.typo.b2.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = TransactionListTheme.colors.pureInverse,
                        textAlign = TextAlign.Start,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                val subtitle = subtitleText(card, category, sourceAccount, targetAccount, tags)
                if (subtitle.isNotBlank()) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = subtitle,
                        style = TransactionListTheme.typo.nC.copy(
                            color = TransactionListTheme.colors.gray,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Start,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            AmountColumn(
                type = card.type,
                amount = card.amount.toDouble(),
                currency = transactionCurrency,
                dueDate = card.dueDate,
                showToAmount = card.type == TransactionListTransactionType.TRANSFER &&
                    toAccountCurrency != transactionCurrency,
                toAmount = card.toAmount.toDouble(),
                toCurrency = toAccountCurrency,
            )
        }

        if (card.dueDate != null) {
            DueDateLabel(card = card)
        }

        if (card.dueDate != null && card.dateTime == null) {
            PayGetSkipRow(
                type = card.type,
                onSkip = { onSkipTransaction(card.id) },
                onPayOrGet = { onPayOrGet(card.id) },
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(1.dp)
                .background(TransactionListTheme.colors.medium)
        )
    }
}

@Composable
private fun TransactionIcon(
    type: TransactionListTransactionType,
    categoryColor: Int?,
    categoryIcon: String?,
    accountColor: Int?,
    accountIcon: String?,
) {
    val backgroundColor: Color
    val iconName: String?
    @DrawableRes val defaultIcon: Int

    when {
        type == TransactionListTransactionType.TRANSFER -> {
            backgroundColor = Ivy
            iconName = null
            defaultIcon = R.drawable.ic_transfer
        }

        categoryColor != null -> {
            backgroundColor = categoryColor.toComposeColor()
            iconName = categoryIcon
            defaultIcon = R.drawable.ic_custom_category_s
        }

        accountColor != null -> {
            backgroundColor = accountColor.toComposeColor()
            iconName = accountIcon
            defaultIcon = R.drawable.ic_custom_account_s
        }

        else -> {
            backgroundColor = Gray
            iconName = null
            defaultIcon = R.drawable.ic_custom_category_s
        }
    }

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        ItemIconSDefaultIcon(
            iconName = iconName,
            defaultIcon = defaultIcon,
            tint = findContrastTextColor(backgroundColor),
        )
    }
}

@Composable
private fun AmountColumn(
    type: TransactionListTransactionType,
    amount: Double,
    currency: String,
    dueDate: java.time.Instant?,
    showToAmount: Boolean,
    toAmount: Double,
    toCurrency: String,
) {
    val timeProvider = LocalTimeProvider.current
    val now = with(LocalTimeConverter.current) { timeProvider.utcNow().toLocalDateTime() }
    val todayStart = timeProvider.localDateNow().atStartOfDay()
    val dueLocal = with(LocalTimeConverter.current) { dueDate?.toLocalDateTime() }

    val amountColor = when (type) {
        TransactionListTransactionType.INCOME -> Green
        TransactionListTransactionType.TRANSFER -> Ivy
        TransactionListTransactionType.EXPENSE -> when {
            dueLocal != null && dueLocal.isAfter(now) -> Orange
            dueLocal != null && dueLocal.isBefore(todayStart) -> Red
            else -> TransactionListTheme.colors.pureInverse
        }
    }
    val sign = when (type) {
        TransactionListTransactionType.INCOME -> "+"
        TransactionListTransactionType.EXPENSE -> "-"
        TransactionListTransactionType.TRANSFER -> ""
    }

    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = "$sign${formatAmount(amount, currency)}",
            style = TransactionListTheme.typo.nB2.copy(
                fontWeight = FontWeight.ExtraBold,
                color = amountColor,
                textAlign = TextAlign.End,
            ),
            maxLines = 1,
        )

        if (showToAmount) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = formatAmount(toAmount, toCurrency),
                style = TransactionListTheme.typo.nC.copy(
                    color = Gray,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.End,
                ),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun DueDateLabel(card: TransactionListTransaction) {
    val dueDate = card.dueDate ?: return
    val timeFormatter = LocalTimeFormatter.current
    val timeProvider = LocalTimeProvider.current

    Text(
        modifier = Modifier.padding(start = 78.dp, end = 20.dp, bottom = 12.dp),
        text = stringResource(
            R.string.due_on,
            with(timeFormatter) {
                dueDate.formatLocal(
                    TimeFormatter.Style.DateOnly(includeWeekDay = true)
                )
            }
        ).uppercase(),
        style = TransactionListTheme.typo.nC.copy(
            color = if (dueDate.isAfter(timeProvider.utcNow())) {
                Orange
            } else {
                TransactionListTheme.colors.gray
            },
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
        ),
    )
}

@Composable
private fun PayGetSkipRow(
    type: TransactionListTransactionType,
    onSkip: () -> Unit,
    onPayOrGet: () -> Unit,
) {
    val isExpense = type == TransactionListTransactionType.EXPENSE

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, bottom = 14.dp),
    ) {
        GradientButton(
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.skip),
            wrapContentMode = false,
            backgroundGradient = Gradient.solid(TransactionListTheme.colors.pure),
            disabledBackgroundColor = TransactionListTheme.colors.gray,
            shape = TransactionListTheme.shapes.rFull,
            textStyle = TransactionListTheme.typo.b2.copy(
                color = TransactionListTheme.colors.pureInverse,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
            ),
            iconTint = White,
        ) {
            onSkip()
        }

        Spacer(Modifier.width(8.dp))

        GradientButton(
            modifier = Modifier.weight(1f),
            text = if (isExpense) stringResource(R.string.pay) else stringResource(R.string.get),
            wrapContentMode = false,
            backgroundGradient = if (isExpense) {
                Gradient(TransactionListTheme.colors.pureInverse, TransactionListTheme.colors.gray)
            } else {
                GradientGreen
            },
            disabledBackgroundColor = TransactionListTheme.colors.gray,
            shape = TransactionListTheme.shapes.rFull,
            textStyle = TransactionListTheme.typo.b2.copy(
                color = if (isExpense) TransactionListTheme.colors.pure else White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
            ),
            iconTint = White,
        ) {
            onPayOrGet()
        }
    }
}

@Composable
private fun primaryText(
    card: TransactionListTransaction,
    category: TransactionListCategory?,
    account: TransactionListAccount?,
): String = when {
    !card.title.isNullOrBlank() -> card.title!!
    card.type == TransactionListTransactionType.TRANSFER -> stringResource(R.string.transfer)
    category != null -> category.name
    account != null -> account.name
    else -> stringResource(R.string.deleted)
}

@Composable
private fun subtitleText(
    card: TransactionListTransaction,
    category: TransactionListCategory?,
    sourceAccount: TransactionListAccount?,
    targetAccount: TransactionListAccount?,
    tags: List<TransactionListTag>,
): String {
    val parts = buildList {
        if (card.type == TransactionListTransactionType.TRANSFER) {
            val from = sourceAccount?.name
            val to = targetAccount?.name
            when {
                from != null && to != null -> add("$from → $to")
                from != null -> add(from)
            }
        } else {
            val titleShown = !card.title.isNullOrBlank()
            when {
                titleShown -> {
                    category?.let { add(it.name) }
                    sourceAccount?.let { add(it.name) }
                }

                category != null -> {
                    sourceAccount?.let { add(it.name) }
                }

                else -> {
                    // account already used as the primary line
                }
            }
        }
        tags.forEach { add("#${it.name}") }
    }
    return parts.joinToString(" · ")
}
