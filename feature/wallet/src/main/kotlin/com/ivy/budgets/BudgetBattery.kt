package com.ivy.budgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.data.model.currency.format
import com.ivy.ui.R
import com.ivy.ui.compose.ResourceIcon
import com.ivy.ui.compose.thenIf
import com.ivy.ui.theme.colors.IvyFixedColors.Ivy
import com.ivy.ui.theme.colors.IvyFixedColors.White
import kotlin.math.abs

@Composable
internal fun BudgetBattery(
    modifier: Modifier = Modifier,
    currency: String,
    expenses: Double,
    budget: Double,
    backgroundNotFilled: Color = BudgetsTheme.colors.pure,
    onClick: (() -> Unit)? = null,
) {
    if (budget == 0.0) return
    val percentSpent = expenses / budget
    val green = BudgetsTheme.colors.green
    val orange = BudgetsTheme.colors.orange
    val red = BudgetsTheme.colors.red

    val textColor = when {
        percentSpent <= 0.30 -> BudgetsTheme.colors.pureInverse
        percentSpent <= 0.50 -> White
        percentSpent <= 0.75 -> White
        else -> White
    }

    val captionTextColor = when {
        percentSpent <= 0.30 -> BudgetsTheme.colors.mediumInverse
        percentSpent <= 0.50 -> White
        percentSpent <= 0.75 -> White
        else -> White
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(BudgetsTheme.shapes.r4)
            .background(backgroundNotFilled)
            .drawBehind {
                drawRect(
                    color = when {
                        percentSpent <= 0.25 -> green
                        percentSpent <= 0.50 -> Ivy
                        percentSpent <= 0.75 -> orange
                        else -> red
                    },
                    size = size.copy(
                        width = (size.width * percentSpent).toFloat()
                    )
                )
            }
            .thenIf(onClick != null) {
                clickable {
                    onClick?.invoke()
                }
            }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(16.dp))

        ResourceIcon(
            icon = if (percentSpent > 1.0) R.drawable.ic_buffer_exceeded else R.drawable.ic_buffer_ok,
            tint = textColor
        )

        Spacer(Modifier.width(16.dp))

        Column {
            Text(
                text = when {
                    percentSpent <= 1 -> stringResource(R.string.left_to_spend)
                    else -> stringResource(R.string.budget_exceeded_by)
                },
                style = BudgetsTheme.typo.c.copy(
                    color = textColor,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Start
                )
            )

            Spacer(Modifier.height(4.dp))

            BudgetAmountCurrencyRow(
                amount = abs(budget - expenses),
                currency = currency,
                textColor = textColor
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = "${expenses.format(currency)}/${budget.format(currency)} $currency",
                style = BudgetsTheme.typo.nC.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = captionTextColor,
                    textAlign = TextAlign.Start
                )
            )
        }
    }
}

@Composable
private fun BudgetAmountCurrencyRow(
    amount: Double,
    currency: String,
    textColor: Color = BudgetsTheme.colors.pureInverse
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = amount.format(currency),
            style = BudgetsTheme.typo.nB2.copy(
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                textAlign = TextAlign.Start
            )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = currency,
            style = BudgetsTheme.typo.nB2.copy(
                fontWeight = FontWeight.Normal,
                color = textColor,
                textAlign = TextAlign.Start
            )
        )
    }
}
