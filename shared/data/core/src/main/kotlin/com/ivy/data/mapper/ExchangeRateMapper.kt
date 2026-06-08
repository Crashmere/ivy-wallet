package com.ivy.data.mapper

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.ivy.data.db.entity.ExchangeRateEntity
import com.ivy.data.model.ExchangeRate
import com.ivy.data.model.primitive.AssetCode
import com.ivy.data.model.primitive.PositiveDouble
import com.ivy.data.remote.responses.ExchangeRatesResponse
import javax.inject.Inject

internal class ExchangeRateMapper @Inject constructor() {
    internal fun ExchangeRateEntity.toDomain(): Either<String, ExchangeRate> = either {
        ExchangeRate(
            baseCurrency = AssetCode.from(baseCurrency).bind(),
            currency = AssetCode.from(currency).bind(),
            rate = PositiveDouble.from(rate).bind(),
            manualOverride = manualOverride,
        )
    }

    internal fun ExchangeRate.toEntity(): ExchangeRateEntity {
        return ExchangeRateEntity(
            baseCurrency = baseCurrency.code,
            currency = currency.code,
            rate = rate.value,
            manualOverride = manualOverride,
        )
    }

    internal fun ExchangeRatesResponse.toDomain(): Either<String, List<ExchangeRate>> = either {
        val domainRates = rates.mapNotNull { (currency, rate) ->
            either {
                ExchangeRate(
                    baseCurrency = AssetCode.EUR,
                    currency = AssetCode.from(currency).bind(),
                    rate = PositiveDouble.from(rate).bind(),
                    manualOverride = false
                )
            }.getOrNull()
        }
        ensure(domainRates.isNotEmpty()) { "Failed to map exchange rates to domain" }
        domainRates
    }
}
