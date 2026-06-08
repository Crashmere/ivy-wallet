package com.ivy.data.remote.responses

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Keep
internal data class ExchangeRatesResponse(
    val date: String,
    @SerialName("eur")
    val rates: Map<String, Double>
)
