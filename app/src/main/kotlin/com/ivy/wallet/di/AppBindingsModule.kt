package com.ivy.wallet.di

import com.ivy.base.time.TimeConverter
import com.ivy.base.time.TimeProvider
import com.ivy.base.time.impl.DeviceTimeProvider
import com.ivy.base.time.impl.StandardTimeConverter
import com.ivy.domain.preferences.toggles.IvyPreferenceToggles
import com.ivy.domain.preferences.toggles.PreferenceToggles
import com.ivy.ui.resource.ResourceProvider
import com.ivy.domain.usecase.reset.ResetWalletDataUseCase
import com.ivy.ui.platform.DatePicker
import com.ivy.ui.platform.FilePicker
import com.ivy.ui.platform.LocaleSettingsLauncher
import com.ivy.wallet.AppStarter
import com.ivy.wallet.IvyAppStarter
import com.ivy.wallet.platform.AndroidLocaleSettingsLauncher
import com.ivy.wallet.platform.AndroidResourceProvider
import com.ivy.wallet.reset.ResetWalletDataUseCaseImpl
import com.ivy.wallet.platform.ActivityDatePicker
import com.ivy.wallet.platform.ActivityResultFilePicker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindingsModule {
    @Binds
    abstract fun appStarter(appStarter: IvyAppStarter): AppStarter

    @Binds
    abstract fun resetWalletDataUseCase(
        resetWalletDataUseCase: ResetWalletDataUseCaseImpl
    ): ResetWalletDataUseCase

    @Binds
    abstract fun filePicker(
        filePicker: ActivityResultFilePicker
    ): FilePicker

    @Binds
    abstract fun datePicker(
        datePicker: ActivityDatePicker
    ): DatePicker

    @Binds
    abstract fun localeSettingsLauncher(
        localeSettingsLauncher: AndroidLocaleSettingsLauncher
    ): LocaleSettingsLauncher

    @Binds
    abstract fun resourceProvider(
        resourceProvider: AndroidResourceProvider
    ): ResourceProvider

    @Binds
    abstract fun preferenceToggles(
        preferenceToggles: IvyPreferenceToggles
    ): PreferenceToggles

    @Binds
    abstract fun timeProvider(
        timeProvider: DeviceTimeProvider
    ): TimeProvider

    @Binds
    abstract fun timeConverter(
        timeConverter: StandardTimeConverter
    ): TimeConverter
}
