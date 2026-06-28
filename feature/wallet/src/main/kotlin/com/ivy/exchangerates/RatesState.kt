package com.ivy.exchangerates

import com.ivy.exchangerates.model.RateUi
import kotlinx.collections.immutable.ImmutableList

internal data class RatesState(
    val baseCurrency: String,
    val manual: ImmutableList<RateUi>,
    val automatic: ImmutableList<RateUi>
)
