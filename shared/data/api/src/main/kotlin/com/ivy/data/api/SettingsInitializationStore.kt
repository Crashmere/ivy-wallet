package com.ivy.data.api

import com.ivy.data.model.Theme

interface SettingsInitializationStore {
    suspend fun ensureInitialized(
        defaultTheme: Theme,
        baseCurrencyCode: String,
        bufferAmount: Double,
    )
}
