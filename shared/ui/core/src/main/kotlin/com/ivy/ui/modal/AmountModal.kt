package com.ivy.ui.modal

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.ui.preferences.LocalAmountInputPreferences
import com.ivy.ui.preferences.asEnabledState
import com.ivy.ui.platform.hideKeyboard
import com.ivy.ui.compose.onCompositionStart
import com.ivy.ui.R
import com.ivy.ui.compose.ResourceIcon
import com.ivy.ui.money.formatAmount
import com.ivy.ui.money.formatAmountInput
import com.ivy.ui.money.formatIntegerAmount
import com.ivy.ui.money.localMoneyDecimalSeparator
import com.ivy.ui.money.parseAmount
import com.ivy.ui.money.parseAmountOrNull
import com.ivy.ui.theme.colors.IvyFixedColors
import java.util.UUID
import kotlin.math.truncate
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@SuppressLint("ComposeModifierMissing")
@Suppress("ParameterNaming")
@Composable
fun BoxWithConstraintsScope.AmountModal(
    id: UUID,
    visible: Boolean,
    currency: String,
    initialAmount: Double?,
    dismiss: () -> Unit,
    showPlusMinus: Boolean = false,
    decimalCountMax: Int = 2,
    Header: (@Composable () -> Unit)? = null,
    amountSpacerTop: Dp = 64.dp,
    onAmountChanged: (Double) -> Unit,
) {
    var amount by remember(id) {
        mutableStateOf(
            if (currency.isNotEmpty()) {
                initialAmount?.takeIf { it != 0.0 }?.let { formatAmount(it, currency) }
                    ?: ""
            } else {
                initialAmount?.takeIf { it != 0.0 }?.let { formatAmount(it, decimalCountMax) }
                    ?: ""
            }
        )
    }

    var calculatorModalVisible by remember(id) {
        mutableStateOf(false)
    }

    IvyModal(
        id = id,
        visible = visible,
        dismiss = dismiss,
        PrimaryAction = {
            ResourceIcon(
                modifier = circleButtonModifier(
                    size = 52.dp,
                    onClick = {
                        calculatorModalVisible = true
                    }
                )
                    .testTag("btn_calculator")
                    .padding(all = 4.dp),
                icon = R.drawable.ic_custom_calculator_m,
                tint = AmountModalTheme.colors.pureInverse
            )

            Spacer(Modifier.width(16.dp))

            ModalPositiveButton(
                text = stringResource(R.string.enter),
                iconStart = R.drawable.ic_check
            ) {
                try {
                    onAmountChanged(parseAmount(amount))
                    dismiss()
                } catch (_: Exception) {
                }
            }
        },
        SecondaryActions = {
            if (showPlusMinus) {
                Row {
                    Spacer(modifier = Modifier.width(24.dp))
                    KeypadCircleButton(
                        text = "+/-",
                        testTag = "plus_minus",
                        fontSize = 22.sp,
                        btnSize = 52.dp,
                        onClick = {
                            when {
                                amount.firstOrNull() == '-' -> {
                                    amount = amount.drop(1)
                                }

                                amount.isNotEmpty() -> {
                                    amount = "-$amount"
                                }
                            }
                        }
                    )
                }
            }
        }
    ) {
        Header?.invoke()

        Spacer(Modifier.height(amountSpacerTop))

        val rootView = LocalView.current
        onCompositionStart {
            rootView.hideKeyboard()
        }

        AmountCurrency(
            amount = amount,
            currency = currency
        )

        Spacer(Modifier.height(10.dp))

        AmountInput(
            currency = currency,
            decimalCountMax = decimalCountMax,
            amount = amount
        ) {
            amount = it
        }

        Spacer(Modifier.height(24.dp))
    }

    CalculatorModal(
        visible = calculatorModalVisible,
        initialAmount = parseAmountOrNull(amount),
        currency = currency,
        dismiss = {
            calculatorModalVisible = false
        },
        onCalculation = {
            amount = if (currency.isNotEmpty()) {
                formatAmount(it, currency)
            } else {
                formatAmount(it, decimalCountMax)
            }
        }
    )
}

@SuppressLint("ComposeModifierMissing")
@Composable
private fun AmountCurrency(
    amount: String,
    currency: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.weight(1f))

        Text(
            text = amount.ifBlank { "0" },
            style = AmountModalTheme.typo.nH2.copy(
                fontWeight = FontWeight.Bold,
                color = AmountModalTheme.colors.pureInverse,
                textAlign = TextAlign.Start
            )
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = currency,
            style = AmountModalTheme.typo.nH2.copy(
                fontWeight = FontWeight.Normal,
                color = AmountModalTheme.colors.pureInverse,
                textAlign = TextAlign.Start
            )
        )

        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun AmountInput(
    currency: String,
    amount: String,
    decimalCountMax: Int = 2,
    setAmount: (String) -> Unit,
) {
    var firstInput by remember { mutableStateOf(true) }

    AmountKeyboard(
        horizontalPadding = 40.dp,
        forCalculator = false,
        onNumberPressed = {
            if (firstInput) {
                setAmount(it)
                firstInput = false
            } else {
                val formattedAmount = formatAmountInput(
                    currencyCode = currency,
                    amount = amount,
                    newSymbol = it,
                    decimalCountMax = decimalCountMax
                )
                if (formattedAmount != null) {
                    setAmount(formattedAmount)
                }
            }
        },
        onDecimalPoint = {
            if (firstInput) {
                setAmount("0${localMoneyDecimalSeparator()}")
                firstInput = false
            } else {
                val newlyEnteredString = if (amount.isEmpty()) {
                    "0${localMoneyDecimalSeparator()}"
                } else {
                    "$amount${localMoneyDecimalSeparator()}"
                }
                if (parseAmountOrNull(newlyEnteredString) != null) {
                    setAmount(newlyEnteredString)
                }
            }
        },
        onBackspace = {
            if (firstInput) {
                setAmount("")
                firstInput = false
            } else {
                if (amount.isNotEmpty()) {
                    val formattedNumber = formatNumber(amount.dropLast(1))
                    setAmount(formattedNumber ?: "")
                }
            }
        }
    )
}

private fun formatNumber(number: String): String? {
    val decimalPartString = number
        .split(localMoneyDecimalSeparator())
        .getOrNull(1)
    val newDecimalCount = decimalPartString?.length ?: 0

    val amountDouble = parseAmountOrNull(number)

    if (newDecimalCount <= 2 && amountDouble != null) {
        val intPart = truncate(amountDouble).toInt()
        val decimalFormatted = if (decimalPartString != null) {
            "${localMoneyDecimalSeparator()}$decimalPartString"
        } else {
            ""
        }

        return formatIntegerAmount(intPart) + decimalFormatted
    }

    return null
}

@SuppressLint(
    "ComposeContentEmitterReturningValues",
    "ComposeMultipleContentEmitters",
    "ComposeModifierMissing",
)
@Suppress("ParameterNaming")
@Composable
internal fun AmountKeyboard(
    forCalculator: Boolean,
    onNumberPressed: (String) -> Unit,
    onDecimalPoint: () -> Unit,
    horizontalPadding: Dp = 0.dp,
    ZeroRow: (@Composable RowScope.() -> Unit)? = null,
    FirstRowExtra: (@Composable RowScope.() -> Unit)? = null,
    SecondRowExtra: (@Composable RowScope.() -> Unit)? = null,
    ThirdRowExtra: (@Composable RowScope.() -> Unit)? = null,
    FourthRowExtra: (@Composable RowScope.() -> Unit)? = null,
    onBackspace: () -> Unit,
) {
    val uiPreferences = LocalAmountInputPreferences.current
    val isStandardLayout = uiPreferences.standardKeypadLayout.asEnabledState()

    // Decide the order of the numbers based on the keypad layout
    val countButtonValues = if (isStandardLayout) {
        listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
        )
    } else { // Calculator like numeric keypad layout
        listOf(
            listOf("7", "8", "9"),
            listOf("4", "5", "6"),
            listOf("1", "2", "3"),
        )
    }

    if (ZeroRow != null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ZeroRow.invoke(this)
        }

        Spacer(Modifier.height(8.dp))
    }

    // Loop through the rows to create CircleNumberButtons
    countButtonValues.forEachIndexed { rowIndex, rowNumbers ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            rowNumbers.forEach { num ->
                CircleNumberButton(
                    forCalculator = forCalculator,
                    value = num,
                    onNumberPressed = onNumberPressed
                )
            }

            when (rowIndex) {
                0 -> FirstRowExtra?.invoke(this)
                1 -> SecondRowExtra?.invoke(this)
                2 -> ThirdRowExtra?.invoke(this)
            }
        }

        Spacer(Modifier.height(8.dp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        KeypadCircleButton(
            text = localMoneyDecimalSeparator(),
            testTag = if (forCalculator) {
                "calc_key_decimal_separator"
            } else {
                "key_decimal_separator"
            }
        ) {
            onDecimalPoint()
        }

        CircleNumberButton(
            forCalculator = forCalculator,
            value = "0",
            onNumberPressed = onNumberPressed
        )

        ResourceIcon(
            modifier = circleButtonModifier(onClick = onBackspace)
                .padding(all = 24.dp)
                .testTag("key_del"),
            icon = R.drawable.ic_backspace,
            tint = IvyFixedColors.Red
        )

        FourthRowExtra?.invoke(this)
    }
}

@Composable
@Suppress("ParameterNaming")
internal fun CircleNumberButton(
    forCalculator: Boolean,
    value: String,
    onNumberPressed: (String) -> Unit,
) {
    KeypadCircleButton(
        text = value,
        testTag = if (forCalculator) {
            "calc_key_$value"
        } else {
            "key_$value"
        },
        onClick = {
            onNumberPressed(value)
        }
    )
}

@SuppressLint("ComposeModifierMissing")
@Composable
internal fun KeypadCircleButton(
    text: String,
    testTag: String,
    textColor: Color = AmountModalTheme.colors.pureInverse,
    fontSize: TextUnit = 32.sp,
    btnSize: Dp = 80.dp,
    onClick: () -> Unit,
) {
    Text(
        modifier = circleButtonModifier(size = btnSize, onClick = onClick)
            .testTag(testTag),
        text = text,
        fontSize = fontSize,
        style = AmountModalTheme.typo.nH2.copy(
            color = textColor,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    )
}

@SuppressLint("ComposableModifierFactory", "ModifierFactoryExtensionFunction")
@Composable
private fun circleButtonModifier(
    size: Dp = 80.dp,
    onClick: () -> Unit,
): Modifier {
    return Modifier
        .size(size)
        .clip(CircleShape)
        .clickable(
            onClick = onClick
        )
        .background(AmountModalTheme.colors.pure, AmountModalTheme.shapes.rFull)
        .border(2.dp, AmountModalTheme.colors.medium, AmountModalTheme.shapes.rFull)
        .wrapContentHeight()
}
