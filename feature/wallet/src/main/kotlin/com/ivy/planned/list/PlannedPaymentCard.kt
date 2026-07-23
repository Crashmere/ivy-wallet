package com.ivy.planned.list

import com.ivy.planned.PlannedTheme

import android.annotation.SuppressLint
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
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.ivy.data.model.TransactionType
import com.ivy.data.model.PlannedPaymentRule
import com.ivy.ui.time.forDisplay
import com.ivy.ui.time.formatDateOnlyWithYear
import com.ivy.ui.R
import com.ivy.ui.icon.ItemIconSDefaultIcon
import com.ivy.ui.money.formatAmount
import com.ivy.ui.theme.colors.IvyFixedColors.Gray
import com.ivy.ui.theme.colors.IvyFixedColors.Green
import com.ivy.ui.theme.colors.IvyFixedColors.Ivy
import com.ivy.ui.theme.colors.findContrastTextColor
import com.ivy.ui.theme.colors.toComposeColor
import kotlinx.collections.immutable.ImmutableList
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

@SuppressLint("ComposeModifierMissing")
@Composable
internal fun LazyItemScope.PlannedPaymentCard(
    baseCurrency: String,
    categories: ImmutableList<PlannedPaymentCategory>,
    accounts: ImmutableList<PlannedPaymentAccount>,
    plannedPayment: PlannedPaymentRule,
    onClick: (PlannedPaymentRule) -> Unit,
    onCategoryClick: (UUID) -> Unit,
    onAccountClick: (UUID) -> Unit,
) {
    val account = accounts.find { it.id == plannedPayment.accountId }
    val currency = account?.currency ?: baseCurrency
    val category = plannedPayment.categoryId?.let { targetId ->
        categories.find { it.id == targetId }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (account != null) {
                    onClick(plannedPayment)
                }
            }
            .testTag("planned_payment_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlannedIcon(
                type = plannedPayment.type,
                categoryColor = category?.color,
                categoryIcon = category?.icon,
            )

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plannedTitle(plannedPayment, category, account),
                    style = PlannedTheme.typo.b2.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = PlannedTheme.colors.pureInverse,
                        textAlign = TextAlign.Start,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                val subtitle = plannedSubtitle(plannedPayment, account)
                if (subtitle.isNotBlank()) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = subtitle,
                        style = PlannedTheme.typo.nC.copy(
                            color = PlannedTheme.colors.gray,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Start,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            PlannedAmount(
                type = plannedPayment.type,
                amount = plannedPayment.amount,
                currency = currency,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(1.dp)
                .background(PlannedTheme.colors.medium)
        )
    }
}

@Composable
private fun PlannedIcon(
    type: TransactionType,
    categoryColor: Int?,
    categoryIcon: String?,
) {
    val backgroundColor: Color
    val iconName: String?
    @DrawableRes val defaultIcon: Int

    when {
        type == TransactionType.TRANSFER -> {
            backgroundColor = Ivy
            iconName = null
            defaultIcon = R.drawable.ic_transfer
        }

        categoryColor != null -> {
            backgroundColor = categoryColor.toComposeColor()
            iconName = categoryIcon
            defaultIcon = R.drawable.ic_custom_category_s
        }

        type == TransactionType.INCOME -> {
            backgroundColor = Green
            iconName = null
            defaultIcon = R.drawable.ic_income
        }

        else -> {
            backgroundColor = Gray
            iconName = null
            defaultIcon = R.drawable.ic_expense
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
private fun PlannedAmount(
    type: TransactionType,
    amount: Double,
    currency: String,
) {
    val color = when (type) {
        TransactionType.INCOME -> Green
        TransactionType.TRANSFER -> Ivy
        TransactionType.EXPENSE -> PlannedTheme.colors.pureInverse
    }
    val sign = when (type) {
        TransactionType.INCOME -> "+"
        TransactionType.EXPENSE -> "-"
        TransactionType.TRANSFER -> ""
    }

    Text(
        text = "$sign${formatAmount(amount, currency)} $currency",
        style = PlannedTheme.typo.nB2.copy(
            fontWeight = FontWeight.ExtraBold,
            color = color,
            textAlign = TextAlign.End,
        ),
        maxLines = 1,
    )
}

@Composable
private fun plannedTitle(
    rule: PlannedPaymentRule,
    category: PlannedPaymentCategory?,
    account: PlannedPaymentAccount?,
): String = when {
    !rule.title.isNullOrBlank() -> rule.title!!
    rule.type == TransactionType.TRANSFER -> stringResource(R.string.transfer)
    category != null -> category.name
    account != null -> account.name
    else -> stringResource(R.string.deleted)
}

@Composable
private fun plannedSubtitle(
    rule: PlannedPaymentRule,
    account: PlannedPaymentAccount?,
): String {
    val parts = buildList {
        add(scheduleText(rule))
        account?.let { add(it.name) }
    }
    return parts.filter { it.isNotBlank() }.joinToString(" · ")
}

@Composable
private fun scheduleText(rule: PlannedPaymentRule): String {
    val startDate = rule.startDate?.toLocalDateTimeInSystemZone()?.toLocalDate()
    return if (rule.oneTime) {
        startDate?.formatDateOnlyWithYear() ?: stringResource(R.string.null_text)
    } else {
        val intervalType = rule.intervalType?.forDisplay(rule.intervalN ?: 0) ?: ""
        stringResource(R.string.repeats_every, rule.intervalN ?: 0, intervalType)
    }
}

private fun Instant.toLocalDateTimeInSystemZone() =
    atZone(ZoneId.systemDefault()).toLocalDateTime()
