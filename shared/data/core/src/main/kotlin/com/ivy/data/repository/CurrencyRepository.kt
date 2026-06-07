package com.ivy.data.repository

import com.ivy.data.api.CurrencyStore
import com.ivy.data.db.dao.read.SettingsDao
import com.ivy.data.db.dao.write.WriteSettingsDao
import com.ivy.data.model.primitive.AssetCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Currency
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyRepository @Inject constructor(
    private val settingsDao: SettingsDao,
    private val writeSettingsDao: WriteSettingsDao,
) : CurrencyStore {
    private var baseCurrencyMemo: AssetCode? = null

    override suspend fun getBaseCurrency(): AssetCode = withContext(Dispatchers.IO) {
        val baseCurrency = baseCurrencyMemo
        if (baseCurrency != null) return@withContext baseCurrency

        val currencyCode = settingsDao.findFirstOrNull()?.currency
            ?: getDefaultFIATCurrency()?.currencyCode
        currencyCode?.let(AssetCode::from)?.getOrNull()
            ?: AssetCode.unsafe(LocalSettingsDefaults.FALLBACK_CURRENCY_CODE)
    }

    override suspend fun getBaseCurrencyCode(): String = getBaseCurrency().code

    private fun getDefaultFIATCurrency(): Currency? {
        return Currency.getInstance(Locale.getDefault())
    }

    override suspend fun setBaseCurrency(newCurrency: AssetCode) {
        withContext(Dispatchers.IO) {
            val currentEntity = settingsDao.findFirstOrNull()
                ?: LocalSettingsDefaults.entity()
            baseCurrencyMemo = newCurrency
            writeSettingsDao.save(
                currentEntity.copy(
                    currency = newCurrency.code
                )
            )
        }
    }
}
