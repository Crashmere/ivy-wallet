package com.ivy.legacy

import com.ivy.base.legacy.SharedPrefs
import com.ivy.base.legacy.Theme
import com.ivy.data.db.dao.read.AccountDao
import com.ivy.data.db.dao.read.SettingsDao
import com.ivy.data.db.dao.write.WriteSettingsDao
import com.ivy.data.repository.CategoryRepository
import com.ivy.legacy.datamodel.Settings
import com.ivy.legacy.utils.ioThread
import com.ivy.wallet.domain.data.IvyCurrency
import com.ivy.wallet.domain.deprecated.logic.PreloadDataLogic
import com.ivy.wallet.domain.deprecated.logic.notification.TransactionReminderLogic
import javax.inject.Inject

@Deprecated("Legacy startup setup. Keep until data initialization is redesigned.")
class InitialDataSetup @Inject constructor(
    private val settingsDao: SettingsDao,
    private val settingsWriter: WriteSettingsDao,
    private val accountDao: AccountDao,
    private val categoryRepository: CategoryRepository,
    private val sharedPrefs: SharedPrefs,
    private val preloadDataLogic: PreloadDataLogic,
    private val transactionReminderLogic: TransactionReminderLogic,
) {
    suspend fun setupDefaults(systemDarkMode: Boolean) {
        ioThread {
            val defaultCurrency = IvyCurrency.getDefault()

            if (settingsDao.findFirstOrNull() == null) {
                settingsWriter.save(
                    Settings(
                        theme = if (systemDarkMode) Theme.DARK else Theme.LIGHT,
                        name = "",
                        baseCurrency = defaultCurrency.code,
                        bufferAmount = 1000.0.toBigDecimal(),
                    ).toEntity()
                )
            }

            if (accountDao.findAll().isEmpty()) {
                preloadDataLogic.preloadAccounts()
            }

            if (categoryRepository.findAll().isEmpty()) {
                preloadDataLogic.preloadCategories()
            }

            sharedPrefs.putBoolean(SharedPrefs.INITIAL_SETUP_COMPLETED, true)
            transactionReminderLogic.scheduleReminder()
        }
    }
}
