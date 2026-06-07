package com.ivy.data.store

import com.ivy.data.api.SettingsStore
import com.ivy.data.model.Theme
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomSettingsStore @Inject constructor(
    private val settingsTable: SettingsTable,
) : SettingsStore {
    override suspend fun getTheme(fallback: Theme): Theme =
        settingsTable.findOrNull()?.theme ?: fallback

    override suspend fun ensureInitialized(
        defaultTheme: Theme,
        currencyCode: String,
        bufferAmount: Double,
    ) {
        settingsTable.ensureInitialized(
            defaultTheme = defaultTheme,
            currencyCode = currencyCode,
            bufferAmount = bufferAmount,
        )
    }

    override suspend fun setTheme(theme: Theme): Theme {
        val currentEntity = settingsTable.findOrDefault()
        settingsTable.save(currentEntity.copy(theme = theme))
        return theme
    }

    override suspend fun getBufferAmount(): BigDecimal =
        settingsTable.findOrNull()?.bufferAmount?.toBigDecimal() ?: BigDecimal.ZERO

    override suspend fun setBufferAmount(amount: BigDecimal): BigDecimal {
        val currentEntity = settingsTable.findOrDefault()
        settingsTable.save(currentEntity.copy(bufferAmount = amount.toDouble()))
        return amount
    }

    override suspend fun deleteAll() {
        settingsTable.deleteAll()
    }
}
