package com.ivy.wallet.domain.startup

import com.ivy.base.legacy.Theme
import com.ivy.data.db.dao.read.AccountDao
import com.ivy.data.db.dao.read.SettingsDao
import com.ivy.data.db.dao.write.WriteSettingsDao
import com.ivy.data.db.entity.SettingsEntity
import com.ivy.data.repository.CategoryRepository
import com.ivy.domain.preferences.AppPreferences
import com.ivy.base.legacy.ioThread
import com.ivy.data.model.currency.IvyCurrency
import com.ivy.wallet.notification.reminder.TransactionReminderLogic
import javax.inject.Inject

@Deprecated("Legacy startup setup. Keep until data initialization is redesigned.")
class InitialDataSetup @Inject constructor(
    private val settingsDao: SettingsDao,
    private val settingsWriter: WriteSettingsDao,
    private val accountDao: AccountDao,
    private val categoryRepository: CategoryRepository,
    private val appPreferences: AppPreferences,
    private val preloadDataLogic: PreloadDataLogic,
    private val transactionReminderLogic: TransactionReminderLogic,
) {
    suspend fun setupDefaults(systemDarkMode: Boolean) {
        ioThread {
            val defaultCurrency = IvyCurrency.getDefault()

            if (settingsDao.findFirstOrNull() == null) {
                settingsWriter.save(
                    SettingsEntity(
                        theme = if (systemDarkMode) Theme.DARK else Theme.LIGHT,
                        name = "",
                        currency = defaultCurrency.code,
                        bufferAmount = 1000.0,
                        isSynced = true,
                        isDeleted = false,
                    )
                )
            }

            if (accountDao.findAll().isEmpty()) {
                preloadDataLogic.preloadAccounts()
            }

            if (categoryRepository.findAll().isEmpty()) {
                preloadDataLogic.preloadCategories()
            }

            appPreferences.initialSetupCompleted = true
            transactionReminderLogic.scheduleReminder()
        }
    }
}
