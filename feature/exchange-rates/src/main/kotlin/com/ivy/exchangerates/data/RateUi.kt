package com.ivy.exchangerates.data

import androidx.compose.runtime.Immutable

@Immutable
internal data class RateUi(
    val from: String,
    val to: String,
    val rate: Double
)
