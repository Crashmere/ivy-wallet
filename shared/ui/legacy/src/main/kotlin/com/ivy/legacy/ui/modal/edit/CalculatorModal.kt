package com.ivy.legacy.ui.modal.edit

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.ui.R
import com.ivy.ui.modal.IvyModal
import com.ivy.ui.modal.ModalSet
import com.ivy.ui.modal.ModalTitle
import com.ivy.ui.money.formatAmount
import com.ivy.ui.money.formatAmountInput
import com.ivy.ui.money.localMoneyDecimalSeparator
import com.ivy.ui.money.normalizeMoneyExpression
import com.ivy.ui.money.parseAmountOrNull
import com.ivy.ui.theme.colors.IvyFixedColors
import com.notkamui.keval.Keval
import java.util.UUID
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@SuppressLint("ComposeModifierMissing")
@Composable
internal fun BoxWithConstraintsScope.CalculatorModal(
      initialAmount: Double?,
      visible: Boolean,
      currency: String,
      dismiss: () -> Unit,
      id: UUID = UUID.randomUUID(),
      onCalculation: (Double) -> Unit
) {
    var expression by remember(id, initialAmount) {
        mutableStateOf(initialAmount?.let { formatAmount(it, currency) } ?: "")
    }

    IvyModal(
        id = id,
        visible = visible,
        dismiss = dismiss,
        PrimaryAction = {
            ModalSet(
                modifier = Modifier.testTag("calc_set")
            ) {
                val result = calculate(expression)
                if (result != null) {
                    onCalculation(result)
                    dismiss()
                }
            }
        }
    ) {
        Spacer(Modifier.height(32.dp))

        ModalTitle(text = stringResource(R.string.calculator))

        Spacer(Modifier.height(32.dp))

        val isEmpty = expression.isBlank()
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            text = if (isEmpty) stringResource(R.string.calculator_empty_expression) else expression,
            style = LegacyTheme.typo.nH2.copy(
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = if (isEmpty) IvyFixedColors.Gray else LegacyTheme.colors.pureInverse
            )
        )

        Spacer(Modifier.height(32.dp))

        AmountKeyboard(
            forCalculator = true,
            ZeroRow = {
                KeypadCircleButton(
                    text = "C",
                    textColor = IvyFixedColors.Red,
                    testTag = "key_C"
                ) {
                    expression = ""
                }

                KeypadCircleButton(
                    text = "(",
                    testTag = "key_("
                ) {
                    expression += "("
                }

                KeypadCircleButton(
                    text = ")",
                    testTag = "key_)"
                ) {
                    expression += ")"
                }

                KeypadCircleButton(
                    text = "÷",
                    testTag = "key_/"
                ) {
                    expression = handleOperator(expression, "÷")
                }
            },
            FirstRowExtra = {
                KeypadCircleButton(
                    text = "×",
                    testTag = "key_*"
                ) {
                    expression = handleOperator(expression, "×")
                }
            },
            SecondRowExtra = {
                KeypadCircleButton(
                    text = "−",
                    testTag = "key_-"
                ) {
                    expression = handleOperator(expression, "−")
                }
            },
            ThirdRowExtra = {
                KeypadCircleButton(
                    text = "+",
                    testTag = "key_+"
                ) {
                    expression = handleOperator(expression, "+")
                }
            },
            FourthRowExtra = {
                KeypadCircleButton(
                    text = "=",
                    testTag = "key_="
                ) {
                    val result = calculate(expression)
                    if (result != null) {
                        expression = formatAmount(result, currency)
                    }
                }
            },

            onNumberPressed = {
                expression = formatExpression(
                    expression = expression + it,
                    currency = currency
                )
            },
            onDecimalPoint = {
                expression = formatExpression(
                    expression = expression + localMoneyDecimalSeparator(),
                    currency = currency
                )
            },
            onBackspace = {
                if (expression.isNotEmpty()) {
                    expression = expression.dropLast(1)
                }
            }
        )

        Spacer(Modifier.height(24.dp))
    }
}

private fun handleOperator(expression: String, operator: String): String {
    return if (expression.isNotEmpty() && expression.last().isOperator()) {
        expression.dropLast(1) + operator
    } else {
        expression + operator
    }
}

private fun Char.isOperator(): Boolean = when (this) {
    '+', '−', '×', '÷' -> true
    else -> false
}

private fun formatExpression(expression: String, currency: String): String {
    var formattedExpression = expression

    expression
        .split("(", ")", "÷", "×", "−", "+")
        .ifEmpty {
            // handle only number expression formatting
            listOf(expression)
        }
        .forEach { part ->
            val numberPart = parseAmountOrNull(part)
            if (numberPart != null) {
                val formattedPart = formatAmountInput(
                    currencyCode = currency,
                    amount = part,
                    newSymbol = ""
                )

                if (formattedPart != null) {
                    formattedExpression = formattedExpression.replace(part, formattedPart)
                }
            }
        }

    return formattedExpression
}

private fun calculate(expression: String): Double? {
    return try {
        // Keval doesn't support negative numbers, so we add a zero in front of the expression
        val expression = buildString {
            for (char in expression) {
                when (char) {
                    '÷' -> this.append('/')
                    '×' -> this.append('*')
                    '−' -> this.append('-')
                    else -> this.append(char)
                }
            }
        }
        val modifiedExpression = if (expression.startsWith("-")) "0$expression" else expression
        Keval.eval(normalizeMoneyExpression(modifiedExpression))
    } catch (e: Exception) {
        null
    }
}
