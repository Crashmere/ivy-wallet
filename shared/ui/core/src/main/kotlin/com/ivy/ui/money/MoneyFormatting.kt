package com.ivy.ui.money

import com.ivy.data.model.currency.IvyCurrency
import com.ivy.data.model.currency.amountToDouble
import com.ivy.data.model.currency.amountToDoubleOrNull
import com.ivy.data.model.currency.format
import com.ivy.data.model.currency.formatInputAmount
import com.ivy.data.model.currency.formatInt
import com.ivy.data.model.currency.localDecimalSeparator
import com.ivy.data.model.currency.normalizeExpression

fun parseAmount(amount: String): Double {
    return amount.amountToDouble()
}

fun parseAmountOrNull(amount: String): Double? {
    return amount.amountToDoubleOrNull()
}

fun normalizeMoneyExpression(expression: String): String {
    return expression.normalizeExpression()
}

fun localMoneyDecimalSeparator(): String {
    return localDecimalSeparator()
}

fun formatAmount(amount: Double, currencyCode: String): String {
    return amount.format(currencyCode)
}

fun formatAmount(amount: Double, decimalPlaces: Int): String {
    return amount.format(decimalPlaces)
}

fun formatIntegerAmount(number: Int): String {
    return formatInt(number)
}

fun formatAmountInput(
    currencyCode: String,
    amount: String,
    newSymbol: String,
    decimalCountMax: Int = 2,
): String? {
    return formatInputAmount(
        currency = currencyCode,
        amount = amount,
        newSymbol = newSymbol,
        decimalCountMax = decimalCountMax
    )
}

fun currencyName(currencyCode: String): String {
    return IvyCurrency.fromCode(currencyCode)?.name ?: ""
}

fun decimalPlacesForCurrency(currencyCode: String): Int {
    return IvyCurrency.getDecimalPlaces(currencyCode)
}
