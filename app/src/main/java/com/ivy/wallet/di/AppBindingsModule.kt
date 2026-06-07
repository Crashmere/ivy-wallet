package com.ivy.wallet.di

import com.ivy.domain.AppStarter
import com.ivy.domain.usecase.ResetWalletDataUseCase
import com.ivy.ui.platform.FilePicker
import com.ivy.wallet.IvyAppStarter
import com.ivy.wallet.domain.reset.ResetWalletDataUseCaseImpl
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
}
