package com.ivy.data.api

import com.ivy.data.model.Theme

interface SettingsStore {
    suspend fun ensureInitialized(
        defaultTheme: Theme,
        baseCurrencyCode: String,
        bufferAmount: Double,
    )

    suspend fun deleteAll()
}
