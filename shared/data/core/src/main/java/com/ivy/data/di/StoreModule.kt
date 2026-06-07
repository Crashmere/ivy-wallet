package com.ivy.data.di

import com.ivy.data.api.AccountStore
import com.ivy.data.api.CategoryStore
import com.ivy.data.api.CurrencyStore
import com.ivy.data.api.ExchangeRateStore
import com.ivy.data.api.SettingsStore
import com.ivy.data.api.TagStore
import com.ivy.data.repository.AccountRepository
import com.ivy.data.repository.CategoryRepository
import com.ivy.data.repository.CurrencyRepository
import com.ivy.data.repository.ExchangeRatesRepository
import com.ivy.data.repository.SettingsRepository
import com.ivy.data.repository.TagRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class StoreModule {
    @Binds
    abstract fun bindAccountStore(repository: AccountRepository): AccountStore

    @Binds
    abstract fun bindCategoryStore(repository: CategoryRepository): CategoryStore

    @Binds
    abstract fun bindCurrencyStore(repository: CurrencyRepository): CurrencyStore

    @Binds
    abstract fun bindSettingsStore(repository: SettingsRepository): SettingsStore

    @Binds
    abstract fun bindExchangeRateStore(repository: ExchangeRatesRepository): ExchangeRateStore

    @Binds
    abstract fun bindTagStore(repository: TagRepository): TagStore
}
