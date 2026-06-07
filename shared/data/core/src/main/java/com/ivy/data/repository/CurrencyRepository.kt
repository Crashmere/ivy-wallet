package com.ivy.data.repository

import android.icu.util.Currency
import com.ivy.base.theme.Theme
import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.api.CurrencyStore
import com.ivy.data.db.dao.read.SettingsDao
import com.ivy.data.db.dao.write.WriteSettingsDao
import com.ivy.data.db.entity.SettingsEntity
import com.ivy.data.model.primitive.AssetCode
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyRepository @Inject constructor(
    private val settingsDao: SettingsDao,
    private val writeSettingsDao: WriteSettingsDao,
    private val dispatchersProvider: DispatchersProvider,
) : CurrencyStore {
    companion object {
        const val FALLBACK_DEFAULT_CURRENCY = "USD"
    }

    private var baseCurrencyMemo: AssetCode? = null

    override suspend fun getBaseCurrency(): AssetCode = withContext(dispatchersProvider.io) {
        val baseCurrency = baseCurrencyMemo
        if (baseCurrency != null) return@withContext baseCurrency

        val currencyCode = settingsDao.findFirstOrNull()?.currency
            ?: getDefaultFIATCurrency()?.currencyCode
        currencyCode?.let(AssetCode::from)?.getOrNull()
            ?: AssetCode.unsafe(FALLBACK_DEFAULT_CURRENCY)
    }

    override suspend fun getBaseCurrencyCode(): String = getBaseCurrency().code

    private fun getDefaultFIATCurrency(): Currency? {
        return Currency.getInstance(Locale.getDefault())
    }

    override suspend fun setBaseCurrency(newCurrency: AssetCode) {
        withContext(dispatchersProvider.io) {
            val currentEntity = settingsDao.findFirstOrNull()
                ?: SettingsEntity(
                    theme = Theme.AUTO,
                    currency = FALLBACK_DEFAULT_CURRENCY,
                    bufferAmount = 0.0,
                    id = UUID.randomUUID()
                )
            baseCurrencyMemo = newCurrency
            writeSettingsDao.save(
                currentEntity.copy(
                    currency = newCurrency.code
                )
            )
        }
    }
}
