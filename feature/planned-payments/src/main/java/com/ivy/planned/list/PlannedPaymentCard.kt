package com.ivy.planned.list

import android.annotation.SuppressLint
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ivy.base.model.TransactionType
import com.ivy.data.model.Category
import com.ivy.data.model.CategoryId
import com.ivy.data.model.IntervalType
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.ui.time.LocalTimeConverter
import com.ivy.legacy.ui.theme.system.LegacyTheme
import com.ivy.legacy.ui.theme.system.style
import com.ivy.legacy.domain.model.Account
import com.ivy.legacy.domain.model.PlannedPaymentRule
import com.ivy.ui.legacy.forDisplay
import com.ivy.legacy.ui.component.transaction.TypeAmountCurrency
import com.ivy.ui.legacy.formatDateOnly
import com.ivy.ui.legacy.formatDateOnlyWithYear
import com.ivy.base.legacy.isNotNullOrBlank
import com.ivy.base.legacy.timeNowUTC
import com.ivy.base.legacy.uppercaseLocal
import com.ivy.navigation.TransactionsScreen
import com.ivy.navigation.navigation
import com.ivy.ui.R
import com.ivy.legacy.ui.theme.Blue
import com.ivy.legacy.ui.theme.Gradient
import com.ivy.legacy.ui.theme.Green
import com.ivy.legacy.ui.theme.Orange
import com.ivy.legacy.ui.component.IvyButton
import com.ivy.legacy.ui.component.IvyIcon
import com.ivy.legacy.ui.component.getCustomIconIdS
import com.ivy.legacy.ui.theme.findContrastTextColor
import com.ivy.legacy.ui.theme.toComposeColor
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

@SuppressLint("ComposeModifierMissing")
@Composable
fun LazyItemScope.PlannedPaymentCard(
    baseCurrency: String,
    categories: ImmutableList<Category>,
    accounts: ImmutableList<Account>,
    plannedPayment: PlannedPaymentRule,
    onClick: (PlannedPaymentRule) -> Unit,
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
            accounts = accounts
        )

        Spacer(Modifier.height(16.dp))

        RuleTextRow(
            oneTime = plannedPayment.oneTime,
            startDate = with(LocalTimeConverter.current) {
                plannedPayment.startDate?.toLocalDateTime()
            },
            intervalN = plannedPayment.intervalN,
            intervalType = plannedPayment.intervalType
        )

        if (plannedPayment.title.isNotNullOrBlank()) {
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

        TypeAmountCurrency(
            transactionType = plannedPayment.type,
            dueDate = null,
            currency = currency,
            amount = plannedPayment.amount
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun PlannedPaymentHeaderRow(
    plannedPayment: PlannedPaymentRule,
    categories: ImmutableList<Category>,
    accounts: ImmutableList<Account>
) {
    val nav = navigation()

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
                plannedPayment.categoryId?.let { targetId -> categories.find { it.id.value == targetId } }
            if (category != null) {
                IvyButton(
                    iconTint = findContrastTextColor(category.color.value.toComposeColor()),
                    iconStart = getCustomIconIdS(
                        category.icon?.id,
                        R.drawable.ic_custom_category_s
                    ),
                    text = category.name.value,
                    backgroundGradient = Gradient.solid(category.color.value.toComposeColor()),
                    textStyle = LegacyTheme.typo.c.style(
                        color = findContrastTextColor(category.color.value.toComposeColor()),
                        fontWeight = FontWeight.ExtraBold
                    ),
                    padding = 8.dp,
                    iconEdgePadding = 10.dp
                ) {
                    nav.navigateTo(
                        TransactionsScreen(
                            accountId = null,
                            categoryId = category.id.value
                        )
                    )
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
                    nav.navigateTo(
                        TransactionsScreen(
                            accountId = account.id,
                            categoryId = null
                        )
                    )
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
                text = startDate?.toLocalDate()?.formatDateOnlyWithYear()?.uppercaseLocal()
                    ?: stringResource(R.string.null_text),
                style = LegacyTheme.typo.nC.style(
                    color = Orange,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        } else {
            val startDateFormatted = startDate?.toLocalDate()?.formatDateOnly()?.uppercaseLocal()
            Text(
                text = stringResource(R.string.starts_date, startDateFormatted ?: ""),
                style = LegacyTheme.typo.nC.style(
                    color = Orange,
                    fontWeight = FontWeight.SemiBold
                )
            )
            val intervalTypeFormatted = intervalType?.forDisplay(intervalN ?: 0)?.uppercaseLocal()
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
