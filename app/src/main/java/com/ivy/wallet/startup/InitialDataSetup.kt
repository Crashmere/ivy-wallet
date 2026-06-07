package com.ivy.wallet.startup

import com.ivy.base.coroutines.ioThread
import com.ivy.data.model.currency.IvyCurrency
import com.ivy.domain.usecase.account.GetAccountsUseCase
import com.ivy.domain.usecase.category.GetCategoriesUseCase
import com.ivy.domain.usecase.settings.EnsureSettingsInitializedUseCase
import com.ivy.domain.usecase.settings.SetInitialSetupCompletedUseCase
import com.ivy.wallet.notification.reminder.TransactionReminderLogic
import javax.inject.Inject

class InitialDataSetup @Inject constructor(
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val ensureSettingsInitialized: EnsureSettingsInitializedUseCase,
    private val setInitialSetupCompleted: SetInitialSetupCompletedUseCase,
    private val preloadDataLogic: PreloadDataLogic,
    private val transactionReminderLogic: TransactionReminderLogic,
) {
    suspend fun setupDefaults(systemDarkMode: Boolean) {
        ioThread {
            val defaultCurrency = IvyCurrency.getDefault()

            ensureSettingsInitialized(
                systemDarkMode = systemDarkMode,
                currencyCode = defaultCurrency.code,
                bufferAmount = 1000.0,
            )

            if (getAccountsUseCase().isEmpty()) {
                preloadDataLogic.preloadAccounts()
            }

            if (getCategoriesUseCase().isEmpty()) {
                preloadDataLogic.preloadCategories()
            }

            setInitialSetupCompleted(true)
            transactionReminderLogic.scheduleReminder()
        }
    }
}
