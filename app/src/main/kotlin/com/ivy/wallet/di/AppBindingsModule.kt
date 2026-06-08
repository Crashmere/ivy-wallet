package com.ivy.wallet.di

import com.ivy.ui.resource.ResourceProvider
import com.ivy.domain.usecase.reset.ResetWalletDataUseCase
import com.ivy.ui.platform.DatePicker
import com.ivy.ui.platform.FilePicker
import com.ivy.ui.platform.LocaleSettingsLauncher
import com.ivy.wallet.platform.AndroidLocaleSettingsLauncher
import com.ivy.wallet.platform.AndroidResourceProvider
import com.ivy.wallet.reset.AppResetWalletDataUseCase
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
    abstract fun resetWalletDataUseCase(
        resetWalletDataUseCase: AppResetWalletDataUseCase
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
}
