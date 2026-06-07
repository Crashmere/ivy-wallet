package com.ivy.wallet.startup

import com.ivy.data.db.dao.read.AccountDao
import com.ivy.data.repository.CategoryRepository
import com.ivy.data.repository.LegacySettingsRepository
import com.ivy.base.coroutines.ioThread
import com.ivy.data.model.currency.IvyCurrency
import com.ivy.domain.preferences.AppPreferences
import com.ivy.wallet.notification.reminder.TransactionReminderLogic
import javax.inject.Inject

class InitialDataSetup @Inject constructor(
    private val accountDao: AccountDao,
    private val categoryRepository: CategoryRepository,
    private val legacySettingsRepository: LegacySettingsRepository,
    private val appPreferences: AppPreferences,
    private val preloadDataLogic: PreloadDataLogic,
    private val transactionReminderLogic: TransactionReminderLogic,
) {
    suspend fun setupDefaults(systemDarkMode: Boolean) {
        ioThread {
            val defaultCurrency = IvyCurrency.getDefault()

            legacySettingsRepository.ensureInitialized(
                systemDarkMode = systemDarkMode,
                currencyCode = defaultCurrency.code,
                bufferAmount = 1000.0,
            )

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
