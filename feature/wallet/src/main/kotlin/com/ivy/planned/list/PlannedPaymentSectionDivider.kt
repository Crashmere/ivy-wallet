package com.ivy.planned.list

import com.ivy.planned.PlannedTheme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.data.model.currency.format
import com.ivy.ui.R
import com.ivy.ui.animation.springBounce
import com.ivy.ui.compose.clickableNoIndication
import com.ivy.ui.compose.rememberInteractionSource

@Composable
internal fun PlannedPaymentSectionDivider(
    expanded: Boolean,
    title: String,
    titleColor: Color,
    baseCurrency: String,
    income: Double,
    expenses: Double,
    setExpanded: (Boolean) -> Unit
) {
    Spacer(Modifier.height(24.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickableNoIndication(rememberInteractionSource()) {
                setExpanded(!expanded)
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        val expandIconRotation by animateFloatAsState(
            targetValue = if (expanded) -180f else 0f,
            animationSpec = springBounce()
        )

        Spacer(Modifier.width(24.dp))

        Column {
            Text(
                modifier = Modifier.testTag("planned_payment_section_title"),
                text = title,
                style = PlannedTheme.typo.b1.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = titleColor,
                    textAlign = TextAlign.Start
                )
            )

            Spacer(Modifier.height(4.dp))

            PlannedPaymentIncomeExpenseRow(
                income = income,
                expenses = expenses,
                baseCurrency = baseCurrency
            )
        }

        Spacer(Modifier.weight(1f))

        Icon(
            modifier = Modifier.rotate(expandIconRotation),
            painter = painterResource(id = R.drawable.ic_expandarrow),
            contentDescription = "icon",
            tint = PlannedTheme.colors.pureInverse
        )

        Spacer(Modifier.width(32.dp))
    }
}

@Composable
private fun PlannedPaymentIncomeExpenseRow(
    income: Double,
    expenses: Double,
    baseCurrency: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (expenses > 0) {
            Text(
                modifier = Modifier.testTag("planned_payment_section_expense"),
                text = "${expenses.format(baseCurrency)} $baseCurrency",
                style = PlannedTheme.typo.nC.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = PlannedTheme.colors.pureInverse,
                    textAlign = TextAlign.Start
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.expenses_lowercase),
                style = PlannedTheme.typo.c.copy(
                    fontWeight = FontWeight.Normal,
                    color = PlannedTheme.colors.pureInverse,
                    textAlign = TextAlign.Start
                )
            )
        }

        if (income > 0 && expenses > 0) {
            Spacer(Modifier.width(8.dp))

            PlannedPaymentSectionDividerDot()

            Spacer(Modifier.width(8.dp))
        }

        if (income > 0) {
            Text(
                modifier = Modifier.testTag("planned_payment_section_income"),
                text = "${income.format(baseCurrency)} $baseCurrency",
                style = PlannedTheme.typo.nC.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = PlannedTheme.colors.green,
                    textAlign = TextAlign.Start
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.income_lowercase),
                style = PlannedTheme.typo.c.copy(
                    fontWeight = FontWeight.Normal,
                    color = PlannedTheme.colors.pureInverse,
                    textAlign = TextAlign.Start
                )
            )
        }
    }
}

@Composable
private fun PlannedPaymentSectionDividerDot() {
    Box(
        modifier = Modifier
            .size(4.dp)
            .background(PlannedTheme.colors.mediumInverse, CircleShape)
    )
}
