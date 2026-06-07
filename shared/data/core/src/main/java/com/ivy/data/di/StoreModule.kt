package com.ivy.data.di

import com.ivy.data.api.CurrencyStore
import com.ivy.data.api.SettingsStore
import com.ivy.data.repository.CurrencyRepository
import com.ivy.data.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class StoreModule {
    @Binds
    abstract fun bindCurrencyStore(repository: CurrencyRepository): CurrencyStore

    @Binds
    abstract fun bindSettingsStore(repository: SettingsRepository): SettingsStore
}
