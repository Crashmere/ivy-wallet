package com.ivy.wallet.reset

import com.ivy.domain.preferences.toggles.PreferenceToggleRepository
import com.ivy.domain.usecase.reset.ResetWalletDataUseCase
import com.ivy.domain.usecase.reset.ClearAppPreferencesUseCase
import com.ivy.domain.usecase.reset.ClearWalletDataUseCase
import com.ivy.domain.usecase.reset.NotifyAllDataChangedUseCase
import com.ivy.ui.navigation.MainScreen
import com.ivy.ui.navigation.Navigation
import com.ivy.wallet.startup.InitialDataSetup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ResetWalletDataUseCaseImpl @Inject constructor(
    private val navigation: Navigation,
    private val clearWalletDataUseCase: ClearWalletDataUseCase,
    private val clearAppPreferences: ClearAppPreferencesUseCase,
    private val notifyAllDataChangedUseCase: NotifyAllDataChangedUseCase,
    private val preferenceToggleRepository: PreferenceToggleRepository,
    private val initialDataSetup: InitialDataSetup,
) : ResetWalletDataUseCase {
    override suspend fun resetAllData() {
        withContext(Dispatchers.IO) {
            clearWalletDataUseCase()
            preferenceToggleRepository.clearAll()
            clearAppPreferences()
        }

        initialDataSetup.setupDefaults(systemDarkMode = false)
        notifyAllDataChangedUseCase()
        navigation.resetBackStack()
        navigation.navigateTo(MainScreen)
    }
}
