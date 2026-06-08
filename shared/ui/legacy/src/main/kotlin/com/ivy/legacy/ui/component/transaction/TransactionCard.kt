package com.ivy.legacy.ui.component.transaction

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ivy.data.model.LegacyTag
import com.ivy.data.model.legacy.LegacyTransaction
import com.ivy.data.model.TransactionType
import com.ivy.data.model.Category
import com.ivy.ui.time.LocalTimeConverter
import com.ivy.ui.time.LocalTimeFormatter
import com.ivy.ui.time.LocalTimeProvider
import com.ivy.legacy.ui.theme.BlueLight
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.legacy.ui.theme.style
import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.data.model.currency.format
import com.ivy.ui.R
import com.ivy.ui.time.TimeFormatter
import com.ivy.data.model.currency.IvyCurrency
import com.ivy.legacy.ui.theme.Gradient
import com.ivy.legacy.ui.theme.GradientGreen
import com.ivy.legacy.ui.theme.GradientIvy
import com.ivy.legacy.ui.theme.GradientOrangeRevert
import com.ivy.legacy.ui.theme.GradientRed
import com.ivy.legacy.ui.theme.Gray
import com.ivy.legacy.ui.theme.Green
import com.ivy.legacy.ui.theme.Ivy
import com.ivy.legacy.ui.theme.Orange
import com.ivy.legacy.ui.theme.Red
import com.ivy.legacy.ui.theme.White
import com.ivy.legacy.ui.component.ItemIconSDefaultIcon
import com.ivy.legacy.ui.component.IvyButton
import com.ivy.legacy.ui.component.IvyIcon
import com.ivy.legacy.ui.theme.findContrastTextColor
import com.ivy.legacy.ui.theme.gradientExpenses
import com.ivy.legacy.ui.theme.toComposeColor
import com.ivy.legacy.ui.component.AmountCurrencyB1
import kotlinx.collections.immutable.ImmutableList
import java.time.LocalDateTime
import java.util.Locale
import java.util.UUID

@Suppress("CyclomaticComplexMethod", "LongMethod")
@Composable
internal fun TransactionCard(
    baseData: TransactionListData,
    transaction: LegacyTransaction,
    shouldShowAccountSpecificColorInTransactions: Boolean,
    onPayOrGet: (UUID) -> Unit,
    modifier: Modifier = Modifier,
    onSkipTransaction: (UUID) -> Unit = {},
    onAccountClick: (UUID) -> Unit,
    onCategoryClick: (UUID) -> Unit,
    onClick: (UUID, TransactionType) -> Unit,
) {
    val sourceAccount = remember(baseData.accounts, transaction.accountId) {
        baseData.accounts.find { it.id == transaction.accountId }
    }
    val targetAccount = remember(baseData.accounts, transaction.toAccountId) {
        baseData.accounts.find { it.id == transaction.toAccountId }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp)
            .clip(LegacyTheme.shapes.r4)
            .clickable {
                if (sourceAccount != null) {
                    onClick(transaction.id, transaction.type)
                }
            }
            .background(LegacyTheme.colors.medium, LegacyTheme.shapes.r4)
            .testTag("transaction_card")
    ) {
        val transactionCurrency = sourceAccount?.currency ?: baseData.baseCurrency
        val toAccountCurrency = targetAccount?.currency ?: baseData.baseCurrency

        Spacer(Modifier.height(20.dp))

        TransactionHeaderRow(
            transaction = transaction,
            categories = baseData.categories,
            accounts = baseData.accounts,
            shouldShowAccountSpecificColorInTransactions = shouldShowAccountSpecificColorInTransactions,
            onAccountClick = onAccountClick,
            onCategoryClick = onCategoryClick
        )

        if (transaction.dueDate != null) {
            Spacer(Modifier.height(12.dp))
            val timeFormatter = LocalTimeFormatter.current
            val timeProvider = LocalTimeProvider.current
            Text(
                modifier = Modifier.padding(horizontal = 24.dp),
                text = stringResource(
                    R.string.due_on,
                    with(timeFormatter) {
                        transaction.dueDate!!.formatLocal(
                            TimeFormatter.Style.DateOnly(
                                includeWeekDay = true
                            )
                        )
                    }
                ).uppercase(),
                style = LegacyTheme.typo.nC.style(
                    color = if (transaction.dueDate!!.isAfter(timeProvider.utcNow())) {
                        Orange
                    } else {
                        LegacyTheme.colors.gray
                    },
                    fontWeight = FontWeight.Bold
                )
            )
        }

        if (transaction.title.isNullOrBlank().not()) {
            Spacer(
                Modifier.height(
                    if (transaction.dueDate != null) 8.dp else 12.dp
                )
            )
            Text(
                modifier = Modifier.padding(horizontal = 24.dp),
                text = transaction.title!!,
                style = LegacyTheme.typo.b1.style(
                    fontWeight = FontWeight.ExtraBold,
                    color = LegacyTheme.colors.pureInverse
                )
            )
        }

        val description = getTransactionDescription(transaction)
        if (!description.isNullOrBlank()) {
            Spacer(Modifier.height(if (transaction.title.isNullOrBlank().not()) 4.dp else 8.dp))
            Text(
                text = description,
                modifier = Modifier.padding(horizontal = 24.dp),
                style = LegacyTheme.typo.nC.style(
                    color = LegacyTheme.colors.gray,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (transaction.dueDate != null) {
            Spacer(Modifier.height(12.dp))
        } else {
            Spacer(Modifier.height(16.dp))
        }

        TypeAmountCurrency(
            transactionType = transaction.type,
            dueDate = with(LocalTimeConverter.current) {
                transaction.dueDate?.toLocalDateTime()
            },
            currency = transactionCurrency,
            amount = transaction.amount.toDouble()
        )

        if (transaction.type == TransactionType.TRANSFER && toAccountCurrency != transactionCurrency) {
            Text(
                modifier = Modifier.padding(start = 68.dp),
                text = "${
                    transaction.toAmount.toDouble()
                        .format(IvyCurrency.getDecimalPlaces(toAccountCurrency))
                } $toAccountCurrency",
                style = LegacyTheme.typo.nB2.style(
                    color = Gray,
                    fontWeight = FontWeight.Normal
                )
            )
        }

        if (transaction.dueDate != null && transaction.dateTime == null) {
            // Pay/Get button
            Spacer(Modifier.height(16.dp))
            val isExpense = transaction.type == TransactionType.EXPENSE
            Row {
                IvyButton(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 24.dp),
                    text = stringResource(R.string.skip),
                    wrapContentMode = false,
                    backgroundGradient = Gradient.solid(LegacyTheme.colors.pure),
                    textStyle = LegacyTheme.typo.b2.style(
                        color = LegacyTheme.colors.pureInverse,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    onSkipTransaction(transaction.id)
                }

                Spacer(Modifier.width(8.dp))

                IvyButton(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 24.dp),
                    text = if (isExpense) stringResource(R.string.pay) else stringResource(R.string.get),
                    wrapContentMode = false,
                    backgroundGradient = if (isExpense) gradientExpenses() else GradientGreen,
                    textStyle = LegacyTheme.typo.b2.style(
                        color = if (isExpense) LegacyTheme.colors.pure else White,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    onPayOrGet(transaction.id)
                }
            }
        }

        if (transaction.tags.isNotEmpty()) {
            TransactionTags(transaction.tags)
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun ColumnScope.TransactionTags(tags: ImmutableList<LegacyTag>) {
    Spacer(Modifier.height(12.dp))

    LazyRow(
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {
        item {
            // Tag Text
            Text(
                text = "Tags:",
                style = LegacyTheme.typo.nC.style(
                    color = LegacyTheme.colors.gray,
                    fontWeight = FontWeight.Normal
                )
            )

            Spacer(modifier = Modifier.width(8.dp))
        }

        items(tags, key = { it.id }) { tag ->
            Text(
                text = "#${tag.name}",
                style = LegacyTheme.typo.nC.style(
                    color = BlueLight,
                    fontWeight = FontWeight.Normal
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TransactionHeaderRow(
    transaction: LegacyTransaction,
    categories: List<Category>,
    accounts: List<LegacyAccount>,
    shouldShowAccountSpecificColorInTransactions: Boolean,
    onAccountClick: (UUID) -> Unit,
    onCategoryClick: (UUID) -> Unit,
) {
    val category = findCategory(
        categoryId = transaction.categoryId,
        categories = categories
    )

    if (transaction.type == TransactionType.TRANSFER) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
        ) {
            if (category != null) {
                CategoryBadgeDisplay(category, onCategoryClick)
                Spacer(modifier = Modifier.height(8.dp))
            }
            TransferHeader(
                accounts = accounts,
                transaction = transaction,
                shouldShowAccountSpecificColorInTransactions = shouldShowAccountSpecificColorInTransactions
            )
        }
    } else {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (category != null) {
                CategoryBadgeDisplay(category, onCategoryClick)
            }

            val account = findAccount(
                accountId = transaction.accountId,
                accounts = accounts
            )

            val accountBackgroundColor = if (shouldShowAccountSpecificColorInTransactions) {
                account?.color?.toComposeColor() ?: LegacyTheme.colors.pure
            } else {
                LegacyTheme.colors.pure
            }

            TransactionBadge(
                text = account?.name ?: stringResource(R.string.deleted),
                backgroundColor = accountBackgroundColor,
                icon = account?.icon,
                defaultIcon = R.drawable.ic_custom_account_s
            ) {
                account?.let {
                    onAccountClick(account.id)
                }
            }
        }
    }
}

private fun findCategory(
    categoryId: UUID?,
    categories: List<Category>
): Category? {
    val targetId = categoryId ?: return null
    return categories.find { it.id.value == targetId }
}

private fun findAccount(
    accountId: UUID?,
    accounts: List<LegacyAccount>
): LegacyAccount? {
    val targetId = accountId ?: return null
    return accounts.find { it.id == targetId }
}

@Composable
private fun CategoryBadgeDisplay(
    category: Category,
    onCategoryClick: (UUID) -> Unit,
) {
    TransactionBadge(
        text = category.name.value,
        backgroundColor = category.color.value.toComposeColor(),
        icon = category.icon?.id,
        defaultIcon = R.drawable.ic_custom_category_s
    ) {
        onCategoryClick(category.id.value)
    }
}

@Composable
private fun getTransactionDescription(transaction: LegacyTransaction): String? {
    val paidFor = with(LocalTimeConverter.current) {
        transaction.paidFor?.toLocalDateTime()
    }
    return when {
        transaction.description.isNullOrBlank().not() -> transaction.description!!
        transaction.recurringRuleId != null &&
                transaction.dueDate == null &&
                paidFor != null -> {
            stringResource(
                R.string.bill_paid,
                paidFor.month.name.lowercase().capitalizeLocal(),
                paidFor.year.toString()
            )
        }

        else -> null
    }
}

@Composable
private fun TransactionBadge(
    text: String,
    backgroundColor: Color,
    icon: String?,
    @DrawableRes
    defaultIcon: Int,

    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(LegacyTheme.shapes.rFull)
            .background(backgroundColor, LegacyTheme.shapes.rFull)
            .clickable {
                onClick()
            }
            .padding(end = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(8.dp))

        val contrastColor = findContrastTextColor(backgroundColor)

        ItemIconSDefaultIcon(
            iconName = icon,
            defaultIcon = defaultIcon,
            tint = contrastColor
        )

        Spacer(Modifier.width(4.dp))

        Text(
            text = text,
            style = LegacyTheme.typo.c.style(
                color = contrastColor,
                fontWeight = FontWeight.ExtraBold
            )
        )

        Spacer(Modifier.width(20.dp))
    }
}

private const val TransferHeaderGradientThreshold = 0.35f

@Composable
private fun TransferHeader(
    accounts: List<LegacyAccount>,
    transaction: LegacyTransaction,
    shouldShowAccountSpecificColorInTransactions: Boolean
) {
    val account = remember(accounts, transaction) {
        accounts.find { transaction.accountId == it.id }
    }
    val toAccount = remember(accounts, transaction) {
        accounts.find { transaction.toAccountId == it.id }
    }

    Row(
        modifier = Modifier
            .then(
                if (shouldShowAccountSpecificColorInTransactions && account != null && toAccount != null) {
                    Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                0f to account.color.toComposeColor(),
                                (TransferHeaderGradientThreshold) to account.color.toComposeColor(),
                                (1f - TransferHeaderGradientThreshold) to toAccount.color.toComposeColor(),
                                1f to toAccount.color.toComposeColor()
                            ),
                            shape = LegacyTheme.shapes.rFull
                        )
                } else {
                    Modifier.background(LegacyTheme.colors.pure, LegacyTheme.shapes.rFull)
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(8.dp))

        val accountContrastColor =
            if (shouldShowAccountSpecificColorInTransactions && account != null) {
                findContrastTextColor(account.color.toComposeColor())
            } else {
                LegacyTheme.colors.pureInverse
            }

        ItemIconSDefaultIcon(
            iconName = account?.icon,
            defaultIcon = R.drawable.ic_custom_account_s,
            tint = accountContrastColor
        )

        Spacer(Modifier.width(4.dp))

        Text(
            modifier = Modifier
                .padding(vertical = 8.dp),
            // used toString() in case of null
            text = account?.name.toString(),
            style = LegacyTheme.typo.c.style(
                fontWeight = FontWeight.ExtraBold,
                color = accountContrastColor
            )
        )

        Spacer(Modifier.width(12.dp))

        IvyIcon(icon = R.drawable.ic_arrow_right, tint = accountContrastColor)

        Spacer(Modifier.width(12.dp))

        val toAccountContrastColor =
            if (shouldShowAccountSpecificColorInTransactions && toAccount != null) {
                findContrastTextColor(toAccount.color.toComposeColor())
            } else {
                LegacyTheme.colors.pureInverse
            }

        ItemIconSDefaultIcon(
            iconName = toAccount?.icon,
            defaultIcon = R.drawable.ic_custom_account_s,
            tint = toAccountContrastColor
        )

        Spacer(Modifier.width(4.dp))

        Text(
            modifier = Modifier
                .padding(vertical = 8.dp),
            // used toString() in case of null
            text = toAccount?.name.toString(),
            style = LegacyTheme.typo.c.style(
                fontWeight = FontWeight.ExtraBold,
                color = toAccountContrastColor
            )
        )

        Spacer(Modifier.width(20.dp))
    }
}

@Composable
fun TypeAmountCurrency(
    transactionType: TransactionType,
    dueDate: LocalDateTime?,
    currency: String,
    amount: Double,
    modifier: Modifier = Modifier
) {
    val timeProvider = LocalTimeProvider.current
    val now = with(LocalTimeConverter.current) { timeProvider.utcNow().toLocalDateTime() }
    val todayStart = timeProvider.localDateNow().atStartOfDay()

    Row(
        modifier = modifier.testTag("type_amount_currency"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(24.dp))

        val style = when (transactionType) {
            TransactionType.INCOME -> {
                AmountTypeStyle(
                    icon = R.drawable.ic_income,
                    gradient = GradientGreen,
                    iconTint = White,
                    textColor = Green
                )
            }

            TransactionType.EXPENSE -> {
                when {
                    dueDate != null && dueDate.isAfter(now) -> {
                        // Upcoming Expense
                        AmountTypeStyle(
                            icon = R.drawable.ic_expense,
                            gradient = GradientOrangeRevert,
                            iconTint = White,
                            textColor = Orange
                        )
                    }

                    dueDate != null && dueDate.isBefore(todayStart) -> {
                        // Overdue Expense
                        AmountTypeStyle(
                            icon = R.drawable.ic_overdue,
                            gradient = GradientRed,
                            iconTint = White,
                            textColor = Red
                        )
                    }

                    else -> {
                        // Normal Expense
                        AmountTypeStyle(
                            icon = R.drawable.ic_expense,
                            gradient = Gradient.black(),
                            iconTint = White,
                            textColor = LegacyTheme.colors.pureInverse
                        )
                    }
                }
            }

            TransactionType.TRANSFER -> {
                // Transfer
                AmountTypeStyle(
                    icon = R.drawable.ic_transfer,
                    gradient = GradientIvy,
                    iconTint = White,
                    textColor = Ivy
                )
            }
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

private data class AmountTypeStyle(
    @DrawableRes val icon: Int,
    val gradient: Gradient,
    val iconTint: Color,
    val textColor: Color
)

private fun String.capitalizeLocal(): String = replaceFirstChar {
    if (it.isLowerCase()) {
        it.titlecase(Locale.getDefault())
    } else {
        it.toString()
    }
}
