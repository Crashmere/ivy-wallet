package com.ivy.data.store

import com.ivy.data.model.Theme
import com.ivy.data.db.entity.SettingsEntity
import java.util.UUID

internal object LocalSettingsDefaults {
    const val FALLBACK_CURRENCY_CODE = "USD"

    fun entity(
        theme: Theme = Theme.AUTO,
        baseCurrencyCode: String = FALLBACK_CURRENCY_CODE,
        bufferAmount: Double = 0.0,
        id: UUID = UUID.randomUUID(),
    ): SettingsEntity = SettingsEntity(
        theme = theme,
        currency = baseCurrencyCode,
        bufferAmount = bufferAmount,
        id = id,
    )
}
