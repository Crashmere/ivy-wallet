package com.ivy.wallet.startup

import com.ivy.data.model.currency.IvyCurrency
import com.ivy.domain.usecase.account.GetAccountsUseCase
import com.ivy.domain.usecase.category.GetCategoriesUseCase
import com.ivy.domain.usecase.settings.EnsureSettingsInitializedUseCase
import com.ivy.domain.usecase.settings.SetInitialSetupCompletedUseCase
import com.ivy.wallet.notification.reminder.TransactionReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class InitialDataSetup @Inject constructor(
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val ensureSettingsInitialized: EnsureSettingsInitializedUseCase,
    private val setInitialSetupCompleted: SetInitialSetupCompletedUseCase,
    private val defaultWalletDataSeeder: DefaultWalletDataSeeder,
    private val transactionReminderScheduler: TransactionReminderScheduler,
) {
    suspend fun setupDefaults(systemDarkMode: Boolean) {
        withContext(Dispatchers.IO) {
            val defaultCurrency = IvyCurrency.getDefault()

            ensureSettingsInitialized(
                systemDarkMode = systemDarkMode,
                currencyCode = defaultCurrency.code,
                bufferAmount = 1000.0,
            )

            if (getAccountsUseCase().isEmpty()) {
                defaultWalletDataSeeder.seedAccounts()
            }

            if (getCategoriesUseCase().isEmpty()) {
                defaultWalletDataSeeder.seedCategories()
            }

            setInitialSetupCompleted(true)
            transactionReminderScheduler.scheduleReminder()
        }
    }
}
