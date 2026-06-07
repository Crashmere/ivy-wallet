package com.ivy.data.store

import com.ivy.data.db.dao.read.SettingsDao
import com.ivy.data.db.dao.write.WriteSettingsDao
import com.ivy.data.db.entity.SettingsEntity
import com.ivy.data.model.Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SettingsTable @Inject constructor(
    private val settingsDao: SettingsDao,
    private val writeSettingsDao: WriteSettingsDao,
) {
    suspend fun findOrNull(): SettingsEntity? = withContext(Dispatchers.IO) {
        settingsDao.findFirstOrNull()
    }

    suspend fun findOrDefault(): SettingsEntity = findOrNull()
        ?: LocalSettingsDefaults.entity()

    suspend fun ensureInitialized(
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

    suspend fun save(entity: SettingsEntity) {
        withContext(Dispatchers.IO) {
            writeSettingsDao.save(entity)
        }
    }

    suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            writeSettingsDao.deleteAll()
        }
    }
}
