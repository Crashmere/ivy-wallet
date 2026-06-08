package com.ivy.wallet.reset

import com.ivy.domain.preferences.toggles.PreferenceToggleService
import com.ivy.domain.usecase.reset.ResetWalletDataUseCase
import com.ivy.domain.usecase.reset.ClearLocalPreferencesUseCase
import com.ivy.domain.usecase.reset.ClearWalletDataUseCase
import com.ivy.domain.usecase.reset.NotifyAllDataChangedUseCase
import com.ivy.wallet.startup.InitialDataSetup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class AppResetWalletDataUseCase @Inject internal constructor(
    private val clearWalletDataUseCase: ClearWalletDataUseCase,
    private val clearLocalPreferences: ClearLocalPreferencesUseCase,
    private val notifyAllDataChangedUseCase: NotifyAllDataChangedUseCase,
    private val preferenceToggleService: PreferenceToggleService,
    private val initialDataSetup: InitialDataSetup,
) : ResetWalletDataUseCase {
    override suspend fun resetAllData() {
        withContext(Dispatchers.IO) {
            clearWalletDataUseCase()
            preferenceToggleService.clearAll()
            clearLocalPreferences()
        }

        initialDataSetup.setupDefaults(systemDarkMode = false)
        notifyAllDataChangedUseCase()
    }
}
