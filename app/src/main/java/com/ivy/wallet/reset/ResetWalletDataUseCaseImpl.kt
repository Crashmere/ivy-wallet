package com.ivy.wallet.reset

import com.ivy.base.coroutines.ioThread
import com.ivy.domain.preferences.AppPreferences
import com.ivy.domain.preferences.DataStorePreferencesRepository
import com.ivy.domain.usecase.ResetWalletDataUseCase
import com.ivy.domain.usecase.reset.ClearWalletDataUseCase
import com.ivy.domain.usecase.reset.NotifyAllDataChangedUseCase
import com.ivy.ui.navigation.MainScreen
import com.ivy.ui.navigation.Navigation
import com.ivy.wallet.startup.InitialDataSetup
import javax.inject.Inject

class ResetWalletDataUseCaseImpl @Inject constructor(
    private val appPreferences: AppPreferences,
    private val navigation: Navigation,
    private val clearWalletDataUseCase: ClearWalletDataUseCase,
    private val notifyAllDataChangedUseCase: NotifyAllDataChangedUseCase,
    private val dataStorePreferencesRepository: DataStorePreferencesRepository,
    private val initialDataSetup: InitialDataSetup,
) : ResetWalletDataUseCase {
    override suspend fun resetAllData() {
        ioThread {
            clearWalletDataUseCase()
            dataStorePreferencesRepository.clearAll()
            appPreferences.clearAll()
        }

        initialDataSetup.setupDefaults(systemDarkMode = false)
        notifyAllDataChangedUseCase()
        navigation.resetBackStack()
        navigation.navigateTo(MainScreen)
    }
}
