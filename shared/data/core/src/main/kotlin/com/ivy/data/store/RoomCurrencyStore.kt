package com.ivy.data.store

import com.ivy.data.api.CurrencyStore
import com.ivy.data.model.primitive.AssetCode
import java.util.Currency
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomCurrencyStore @Inject constructor(
    private val settingsTable: SettingsTable,
) : CurrencyStore {
    private var baseCurrencyMemo: AssetCode? = null

    override suspend fun getBaseCurrency(): AssetCode {
        val baseCurrency = baseCurrencyMemo
        if (baseCurrency != null) return baseCurrency

        val currencyCode = settingsTable.findOrNull()?.currency
            ?: getDefaultFIATCurrency()?.currencyCode
        return currencyCode?.let(AssetCode::from)?.getOrNull()
            ?: AssetCode.unsafe(LocalSettingsDefaults.FALLBACK_CURRENCY_CODE)
    }

    override suspend fun getBaseCurrencyCode(): String = getBaseCurrency().code

    private fun getDefaultFIATCurrency(): Currency? {
        return Currency.getInstance(Locale.getDefault())
    }

    override suspend fun setBaseCurrency(newCurrency: AssetCode) {
        val currentEntity = settingsTable.findOrDefault()
        baseCurrencyMemo = newCurrency
        settingsTable.save(
            currentEntity.copy(
                currency = newCurrency.code
            )
        )
    }
}
