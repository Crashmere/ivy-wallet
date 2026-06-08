package com.ivy.ui.summary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.data.model.currency.IvyCurrency
import com.ivy.data.model.currency.format
import com.ivy.ui.R
import com.ivy.ui.compose.GradientButton
import com.ivy.ui.compose.drawColoredShadow
import com.ivy.ui.theme.colors.Gradient
import com.ivy.ui.theme.colors.IvyFixedColors
import com.ivy.ui.theme.colors.findContrastTextColor
import com.ivy.ui.theme.colors.isDarkColor

@Suppress("ParameterNaming")
@Composable
fun IncomeExpensesCards(
    currency: String,
    income: Double,
    expenses: Double,
    incomeTransactionCount: Int,
    expenseTransactionCount: Int,

    hasAddButtons: Boolean,
    itemColor: Color,

    incomeHeaderCardClicked: () -> Unit = {},
    expenseHeaderCardClicked: () -> Unit = {},
    onAddIncome: () -> Unit = {},
    onAddExpense: () -> Unit = {},
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(16.dp))

        HeaderCard(
            title = stringResource(R.string.income_uppercase),
            currencyCode = currency,
            amount = income,
            transactionCount = incomeTransactionCount,
            addButtonText = if (hasAddButtons) stringResource(R.string.add_income) else null,
            isIncome = true,
            itemColor = itemColor,
            onHeaderCardClicked = { incomeHeaderCardClicked() }
        ) {
            onAddIncome()
        }

        Spacer(Modifier.width(12.dp))

        HeaderCard(
            title = stringResource(R.string.expenses_uppercase),
            currencyCode = currency,
            amount = expenses,
            transactionCount = expenseTransactionCount,
            addButtonText = if (hasAddButtons) stringResource(R.string.add_expense) else null,
            isIncome = false,
            itemColor = itemColor,
            onHeaderCardClicked = { expenseHeaderCardClicked() }
        ) {
            onAddExpense()
        }

        Spacer(Modifier.width(16.dp))
    }
}

@Composable
@Suppress("ParameterNaming")
private fun RowScope.HeaderCard(
    title: String,
    currencyCode: String,
    amount: Double,
    transactionCount: Int,

    isIncome: Boolean,
    addButtonText: String?,

    itemColor: Color,

    onHeaderCardClicked: () -> Unit = {},
    onAddClick: () -> Unit
) {
    val summaryTheme = IncomeExpenseSummaryTheme
    val backgroundColor = if (isDarkColor(itemColor)) {
        summaryTheme.colors.mediumBlack.copy(alpha = 0.9f)
    } else {
        summaryTheme.colors.mediumWhite.copy(alpha = 0.9f)
    }

    val contrastColor = findContrastTextColor(backgroundColor)

    Column(
        modifier = Modifier
            .weight(1f)
            .drawColoredShadow(
                color = backgroundColor,
                alpha = 0.1f
            )
            .background(backgroundColor, summaryTheme.shapes.r2)
            .clickable { onHeaderCardClicked() },
    ) {
        Spacer(Modifier.height(24.dp))

        Text(
            modifier = Modifier.padding(horizontal = 24.dp),
            text = title,
            style = summaryTheme.typo.c.copy(
                color = contrastColor,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Start
            )
        )

        Spacer(Modifier.height(12.dp))

        Text(
            modifier = Modifier.padding(horizontal = 24.dp),
            text = amount.format(currencyCode),
            style = summaryTheme.typo.nB1.copy(
                color = contrastColor,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Start
            )
        )
        Text(
            modifier = Modifier.padding(horizontal = 24.dp),
            text = IvyCurrency.fromCode(currencyCode)?.name ?: "",
            style = summaryTheme.typo.b2.copy(
                color = contrastColor,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Start
            )
        )

        Spacer(Modifier.height(12.dp))

        Text(
            modifier = Modifier.padding(horizontal = 24.dp),
            text = transactionCount.toString(),
            style = summaryTheme.typo.nB1.copy(
                color = contrastColor,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Start
            )
        )
        Text(
            modifier = Modifier.padding(horizontal = 24.dp),
            text = stringResource(R.string.transactions),
            style = summaryTheme.typo.b2.copy(
                color = contrastColor,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Start
            )
        )

        Spacer(Modifier.height(24.dp))

        if (addButtonText != null) {
            val addButtonBackground = if (isIncome) {
                summaryTheme.colors.green
            } else {
                contrastColor
            }
            GradientButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .align(Alignment.CenterHorizontally),
                text = addButtonText,
                shadowAlpha = 0.1f,
                backgroundGradient = Gradient.solid(addButtonBackground),
                disabledBackgroundColor = summaryTheme.colors.gray,
                shape = summaryTheme.shapes.rFull,
                textStyle = summaryTheme.typo.b2.copy(
                    color = findContrastTextColor(addButtonBackground),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    fontSize = 12.sp
                ),
                iconTint = IvyFixedColors.White,
                wrapContentMode = false
            ) {
                onAddClick()
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}
