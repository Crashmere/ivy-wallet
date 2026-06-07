package com.ivy.data.repository

import com.ivy.base.theme.Theme
import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.read.SettingsDao
import com.ivy.data.db.dao.write.WriteSettingsDao
import com.ivy.data.db.entity.SettingsEntity
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val settingsDao: SettingsDao,
    private val writeSettingsDao: WriteSettingsDao,
    private val dispatchersProvider: DispatchersProvider,
) {
    suspend fun getTheme(fallback: Theme = Theme.AUTO): Theme = withContext(dispatchersProvider.io) {
        settingsDao.findFirstOrNull()?.theme ?: fallback
    }

    suspend fun getTheme(systemDarkMode: Boolean): Theme = getTheme(
        fallback = if (systemDarkMode) Theme.DARK else Theme.LIGHT
    )

    suspend fun ensureInitialized(
        systemDarkMode: Boolean,
        currencyCode: String,
        bufferAmount: Double,
    ) {
        withContext(dispatchersProvider.io) {
            if (settingsDao.findFirstOrNull() == null) {
                writeSettingsDao.save(
                    SettingsEntity(
                        theme = if (systemDarkMode) Theme.DARK else Theme.LIGHT,
                        currency = currencyCode,
                        bufferAmount = bufferAmount,
                    )
                )
            }
        }
    }

    suspend fun switchTheme(): Theme = withContext(dispatchersProvider.io) {
        val currentEntity = settingsEntityOrDefault()
        val newTheme = currentEntity.theme.next()
        writeSettingsDao.save(currentEntity.copy(theme = newTheme))
        newTheme
    }

    suspend fun getBufferAmount(): BigDecimal = withContext(dispatchersProvider.io) {
        settingsDao.findFirstOrNull()?.bufferAmount?.toBigDecimal() ?: BigDecimal.ZERO
    }

    suspend fun setBufferAmount(amount: BigDecimal): BigDecimal = withContext(dispatchersProvider.io) {
        val currentEntity = settingsEntityOrDefault()
        writeSettingsDao.save(currentEntity.copy(bufferAmount = amount.toDouble()))
        amount
    }

    private suspend fun settingsEntityOrDefault(): SettingsEntity {
        return settingsDao.findFirstOrNull()
            ?: SettingsEntity(
                theme = Theme.AUTO,
                currency = CurrencyRepository.FALLBACK_DEFAULT_CURRENCY,
                bufferAmount = 0.0,
                id = UUID.randomUUID()
            )
    }

    private fun Theme.next(): Theme = when (this) {
        Theme.LIGHT -> Theme.DARK
        Theme.DARK -> Theme.AMOLED_DARK
        Theme.AMOLED_DARK -> Theme.AUTO
        Theme.AUTO -> Theme.LIGHT
    }
}
