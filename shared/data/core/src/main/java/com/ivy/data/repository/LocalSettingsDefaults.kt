package com.ivy.data.repository

import com.ivy.base.theme.Theme
import com.ivy.data.db.entity.SettingsEntity
import java.util.UUID

internal object LocalSettingsDefaults {
    const val FALLBACK_CURRENCY_CODE = "USD"

    fun entity(
        theme: Theme = Theme.AUTO,
        currencyCode: String = FALLBACK_CURRENCY_CODE,
        bufferAmount: Double = 0.0,
        id: UUID = UUID.randomUUID(),
    ): SettingsEntity = SettingsEntity(
        theme = theme,
        currency = currencyCode,
        bufferAmount = bufferAmount,
        id = id,
    )
}
