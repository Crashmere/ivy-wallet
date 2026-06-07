package com.ivy.data.repository

import com.ivy.data.api.SettingsStore
import com.ivy.data.db.dao.read.SettingsDao
import com.ivy.data.db.dao.write.WriteSettingsDao
import com.ivy.data.db.entity.SettingsEntity
import com.ivy.data.model.Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val settingsDao: SettingsDao,
    private val writeSettingsDao: WriteSettingsDao,
) : SettingsStore {
    override suspend fun getTheme(fallback: Theme): Theme = withContext(Dispatchers.IO) {
        settingsDao.findFirstOrNull()?.theme ?: fallback
    }

    override suspend fun ensureInitialized(
        defaultTheme: Theme,
        currencyCode: String,
        bufferAmount: Double,
    ) {
        withContext(Dispatchers.IO) {
            if (settingsDao.findFirstOrNull() == null) {
                writeSettingsDao.save(
                    LocalSettingsDefaults.entity(
                        theme = defaultTheme,
                        currencyCode = currencyCode,
                        bufferAmount = bufferAmount,
                    )
                )
            }
        }
    }

    override suspend fun setTheme(theme: Theme): Theme = withContext(Dispatchers.IO) {
        val currentEntity = settingsEntityOrDefault()
        writeSettingsDao.save(currentEntity.copy(theme = theme))
        theme
    }

    override suspend fun getBufferAmount(): BigDecimal = withContext(Dispatchers.IO) {
        settingsDao.findFirstOrNull()?.bufferAmount?.toBigDecimal() ?: BigDecimal.ZERO
    }

    override suspend fun setBufferAmount(amount: BigDecimal): BigDecimal = withContext(Dispatchers.IO) {
        val currentEntity = settingsEntityOrDefault()
        writeSettingsDao.save(currentEntity.copy(bufferAmount = amount.toDouble()))
        amount
    }

    override suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            writeSettingsDao.deleteAll()
        }
    }

    private suspend fun settingsEntityOrDefault(): SettingsEntity {
        return settingsDao.findFirstOrNull()
            ?: LocalSettingsDefaults.entity()
    }
}
