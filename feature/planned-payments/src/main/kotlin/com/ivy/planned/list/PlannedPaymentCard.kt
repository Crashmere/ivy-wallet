package com.ivy.planned.list

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import com.ivy.data.model.TransactionType
import com.ivy.data.model.IntervalType
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.legacy.ui.theme.style
import com.ivy.data.model.PlannedPaymentRule
import com.ivy.ui.time.forDisplay
import com.ivy.ui.time.formatDateOnly
import com.ivy.ui.time.formatDateOnlyWithYear
import com.ivy.ui.R
import com.ivy.legacy.ui.component.AmountCurrencyB1
import com.ivy.legacy.ui.theme.Gradient
import com.ivy.legacy.ui.theme.GradientGreen
import com.ivy.legacy.ui.theme.GradientIvy
import com.ivy.legacy.ui.theme.Green
import com.ivy.legacy.ui.theme.Ivy
import com.ivy.legacy.ui.theme.Orange
import com.ivy.legacy.ui.theme.White
import com.ivy.legacy.ui.component.IvyButton
import com.ivy.legacy.ui.component.IvyIcon
import com.ivy.ui.icon.getCustomIconIdS
import com.ivy.legacy.ui.theme.findContrastTextColor
import com.ivy.legacy.ui.theme.toComposeColor
import kotlinx.collections.immutable.ImmutableList
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale
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
    Spacer(Modifier.height(12.dp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(LegacyTheme.shapes.r4)
            .clickable {
                if (accounts.find { it.id == plannedPayment.accountId } != null) {
                    onClick(plannedPayment)
                }
            }
            .background(LegacyTheme.colors.medium, LegacyTheme.shapes.r4)
            .testTag("planned_payment_card")
    ) {
        val currency = accounts.find { it.id == plannedPayment.accountId }?.currency ?: baseCurrency

        Spacer(Modifier.height(20.dp))

        PlannedPaymentHeaderRow(
            plannedPayment = plannedPayment,
            categories = categories,
            accounts = accounts,
            onCategoryClick = onCategoryClick,
            onAccountClick = onAccountClick
        )

        Spacer(Modifier.height(16.dp))

        RuleTextRow(
            oneTime = plannedPayment.oneTime,
            startDate = plannedPayment.startDate?.toLocalDateTimeInSystemZone(),
            intervalN = plannedPayment.intervalN,
            intervalType = plannedPayment.intervalType
        )

        if (plannedPayment.title.isNullOrBlank().not()) {
            Spacer(Modifier.height(8.dp))

            Text(
                modifier = Modifier.padding(horizontal = 24.dp),
                text = plannedPayment.title!!,
                style = LegacyTheme.typo.b1.style(
                    fontWeight = FontWeight.ExtraBold,
                    color = LegacyTheme.colors.pureInverse
                )
            )
        }

        Spacer(Modifier.height(20.dp))

        PlannedPaymentAmountRow(
            transactionType = plannedPayment.type,
            currency = currency,
            amount = plannedPayment.amount
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun PlannedPaymentAmountRow(
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
            TransactionType.INCOME -> PlannedAmountTypeStyle(
                icon = R.drawable.ic_income,
                gradient = GradientGreen,
                iconTint = White,
                textColor = Green
            )

            TransactionType.EXPENSE -> PlannedAmountTypeStyle(
                icon = R.drawable.ic_expense,
                gradient = Gradient.black(),
                iconTint = White,
                textColor = LegacyTheme.colors.pureInverse
            )

            TransactionType.TRANSFER -> PlannedAmountTypeStyle(
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

private data class PlannedAmountTypeStyle(
    @DrawableRes val icon: Int,
    val gradient: Gradient,
    val iconTint: Color,
    val textColor: Color
)

private fun Instant.toLocalDateTimeInSystemZone() =
    atZone(ZoneId.systemDefault()).toLocalDateTime()

@Composable
private fun PlannedPaymentHeaderRow(
    plannedPayment: PlannedPaymentRule,
    categories: ImmutableList<PlannedPaymentCategory>,
    accounts: ImmutableList<PlannedPaymentAccount>,
    onCategoryClick: (UUID) -> Unit,
    onAccountClick: (UUID) -> Unit,
) {
    if (plannedPayment.type != TransactionType.TRANSFER) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(20.dp))

            IvyIcon(
                modifier = Modifier
                    .background(LegacyTheme.colors.pure, CircleShape),
                icon = R.drawable.ic_planned_payments,
                tint = LegacyTheme.colors.pureInverse
            )

            Spacer(Modifier.width(12.dp))

            val category =
                plannedPayment.categoryId?.let { targetId -> categories.find { it.id == targetId } }
            if (category != null) {
                IvyButton(
                    iconTint = findContrastTextColor(category.color.toComposeColor()),
                    iconStart = getCustomIconIdS(
                        category.icon,
                        R.drawable.ic_custom_category_s
                    ),
                    text = category.name,
                    backgroundGradient = Gradient.solid(category.color.toComposeColor()),
                    textStyle = LegacyTheme.typo.c.style(
                        color = findContrastTextColor(category.color.toComposeColor()),
                        fontWeight = FontWeight.ExtraBold
                    ),
                    padding = 8.dp,
                    iconEdgePadding = 10.dp
                ) {
                    onCategoryClick(category.id)
                }

                Spacer(Modifier.width(12.dp))
            }

            val account = accounts.find { it.id == plannedPayment.accountId }
            IvyButton(
                backgroundGradient = Gradient.solid(LegacyTheme.colors.pure),
                text = account?.name ?: stringResource(R.string.deleted),
                iconTint = LegacyTheme.colors.pureInverse,
                iconStart = getCustomIconIdS(account?.icon, R.drawable.ic_custom_account_s),
                textStyle = LegacyTheme.typo.c.style(
                    color = LegacyTheme.colors.pureInverse,
                    fontWeight = FontWeight.ExtraBold
                ),
                padding = 8.dp,
                iconEdgePadding = 10.dp
            ) {
                account?.let {
                    onAccountClick(account.id)
                }
            }
        }
    }
}

@Composable
private fun RuleTextRow(
    oneTime: Boolean,
    startDate: LocalDateTime?,
    intervalN: Int?,
    intervalType: IntervalType?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(24.dp))

        if (oneTime) {
            Text(
                text = stringResource(R.string.planned_for_uppercase),
                style = LegacyTheme.typo.nC.style(
                    color = Orange,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                modifier = Modifier.padding(bottom = 1.dp),
                text = startDate?.toLocalDate()?.formatDateOnlyWithYear()?.uppercase(Locale.getDefault())
                    ?: stringResource(R.string.null_text),
                style = LegacyTheme.typo.nC.style(
                    color = Orange,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        } else {
            val startDateFormatted = startDate?.toLocalDate()?.formatDateOnly()?.uppercase(Locale.getDefault())
            Text(
                text = stringResource(R.string.starts_date, startDateFormatted ?: ""),
                style = LegacyTheme.typo.nC.style(
                    color = Orange,
                    fontWeight = FontWeight.SemiBold
                )
            )
            val intervalTypeFormatted = intervalType?.forDisplay(intervalN ?: 0)?.uppercase(Locale.getDefault())
            Text(
                modifier = Modifier.padding(bottom = 1.dp),
                text = stringResource(
                    R.string.repeats_every,
                    intervalN ?: 0,
                    intervalTypeFormatted ?: ""
                ),
                style = LegacyTheme.typo.nC.style(
                    color = Orange,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }

        Spacer(Modifier.width(24.dp))
    }
}
