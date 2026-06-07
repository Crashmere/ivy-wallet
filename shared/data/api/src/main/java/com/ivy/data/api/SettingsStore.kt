package com.ivy.data.api

import com.ivy.base.theme.Theme
import java.math.BigDecimal

interface SettingsStore {
    suspend fun getTheme(fallback: Theme = Theme.AUTO): Theme

    suspend fun getTheme(systemDarkMode: Boolean): Theme

    suspend fun ensureInitialized(
        systemDarkMode: Boolean,
        currencyCode: String,
        bufferAmount: Double,
    )

    suspend fun switchTheme(): Theme

    suspend fun getBufferAmount(): BigDecimal

    suspend fun setBufferAmount(amount: BigDecimal): BigDecimal

    suspend fun deleteAll()
}
