package com.ivy.legacy.ui.component

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
import androidx.compose.ui.unit.dp
import com.ivy.legacy.ui.theme.system.LegacyTheme
import com.ivy.legacy.ui.theme.system.style
import com.ivy.ui.compose.thenIf
import com.ivy.data.model.currency.format
import com.ivy.ui.R
import com.ivy.legacy.ui.theme.Green
import com.ivy.legacy.ui.theme.Ivy
import com.ivy.legacy.ui.theme.Orange
import com.ivy.legacy.ui.theme.Red
import com.ivy.legacy.ui.theme.White
import com.ivy.legacy.ui.component.IvyIcon
import com.ivy.legacy.ui.component.AmountCurrencyB2Row
import kotlin.math.abs

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun BudgetBattery(
    modifier: Modifier = Modifier,
    currency: String,
    expenses: Double,
    budget: Double,
    backgroundNotFilled: Color = LegacyTheme.colors.pure,
    onClick: (() -> Unit)? = null,
) {
    if (budget == 0.0) return
    val percentSpent = expenses / budget

    val textColor = when {
        percentSpent <= 0.30 -> {
            LegacyTheme.colors.pureInverse
        }

        percentSpent <= 0.50 -> {
            White
        }

        percentSpent <= 0.75 -> {
            White
        }

        else -> White
    }

    val captionTextColor = when {
        percentSpent <= 0.30 -> {
            LegacyTheme.colors.mediumInverse
        }

        percentSpent <= 0.50 -> {
            White
        }

        percentSpent <= 0.75 -> {
            White
        }

        else -> White
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(LegacyTheme.shapes.r4)
            .background(backgroundNotFilled)
            .drawBehind {
                drawRect(
                    color = when {
                        percentSpent <= 0.25 -> {
                            Green
                        }

                        percentSpent <= 0.50 -> {
                            Ivy
                        }

                        percentSpent <= 0.75 -> {
                            Orange
                        }

                        else -> Red
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

        IvyIcon(
            icon = if (percentSpent > 1.0) R.drawable.ic_buffer_exceeded else R.drawable.ic_buffer_ok,
            tint = textColor
        )

        Spacer(Modifier.width(16.dp))

        Column {
            Text(
                text = when {
                    percentSpent <= 1 -> {
                        stringResource(R.string.left_to_spend)
                    }

                    else -> stringResource(R.string.budget_exceeded_by)
                },
                style = LegacyTheme.typo.c.style(
                    color = textColor,
                    fontWeight = FontWeight.ExtraBold
                )
            )

            Spacer(Modifier.height(4.dp))

            AmountCurrencyB2Row(
                amount = abs(budget - expenses),
                currency = currency,
                textColor = textColor
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = "${expenses.format(currency)}/${budget.format(currency)} $currency",
                style = LegacyTheme.typo.nC.style(
                    fontWeight = FontWeight.ExtraBold,
                    color = captionTextColor
                )
            )
        }
    }
}
