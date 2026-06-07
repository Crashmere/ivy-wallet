package com.ivy.data.api

import com.ivy.data.model.Theme
import java.math.BigDecimal

interface SettingsStore {
    suspend fun getTheme(fallback: Theme = Theme.AUTO): Theme

    suspend fun ensureInitialized(
        defaultTheme: Theme,
        currencyCode: String,
        bufferAmount: Double,
    )

    suspend fun setTheme(theme: Theme): Theme

    suspend fun getBufferAmount(): BigDecimal

    suspend fun setBufferAmount(amount: BigDecimal): BigDecimal

    suspend fun deleteAll()
}
