package com.ivy.data.di

import com.ivy.data.DataObserver
import com.ivy.data.api.AccountStore
import com.ivy.data.api.BudgetStore
import com.ivy.data.api.CategoryStore
import com.ivy.data.api.CurrencyStore
import com.ivy.data.api.DataChangePublisher
import com.ivy.data.api.ExchangeRateStore
import com.ivy.data.api.LoanRecordStore
import com.ivy.data.api.LoanStore
import com.ivy.data.api.PlannedPaymentRuleStore
import com.ivy.data.api.PreferenceToggleStore
import com.ivy.data.api.SettingsStore
import com.ivy.data.api.TagStore
import com.ivy.data.api.TransactionStore
import com.ivy.data.repository.AccountRepository
import com.ivy.data.repository.BudgetStoreImpl
import com.ivy.data.repository.CategoryRepository
import com.ivy.data.repository.CurrencyRepository
import com.ivy.data.repository.ExchangeRatesRepository
import com.ivy.data.repository.LoanRecordStoreImpl
import com.ivy.data.repository.LoanStoreImpl
import com.ivy.data.repository.PlannedPaymentRuleStoreImpl
import com.ivy.data.repository.SettingsRepository
import com.ivy.data.repository.TagRepository
import com.ivy.data.repository.TransactionRepository
import com.ivy.data.datastore.DataStorePreferenceToggleStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class StoreModule {
    @Binds
    abstract fun bindDataChangePublisher(observer: DataObserver): DataChangePublisher

    @Binds
    abstract fun bindAccountStore(repository: AccountRepository): AccountStore

    @Binds
    abstract fun bindBudgetStore(store: BudgetStoreImpl): BudgetStore

    @Binds
    abstract fun bindCategoryStore(repository: CategoryRepository): CategoryStore

    @Binds
    abstract fun bindCurrencyStore(repository: CurrencyRepository): CurrencyStore

    @Binds
    abstract fun bindSettingsStore(repository: SettingsRepository): SettingsStore

    @Binds
    abstract fun bindExchangeRateStore(repository: ExchangeRatesRepository): ExchangeRateStore

    @Binds
    abstract fun bindLoanStore(store: LoanStoreImpl): LoanStore

    @Binds
    abstract fun bindLoanRecordStore(store: LoanRecordStoreImpl): LoanRecordStore

    @Binds
    abstract fun bindPlannedPaymentRuleStore(
        store: PlannedPaymentRuleStoreImpl
    ): PlannedPaymentRuleStore

    @Binds
    abstract fun bindPreferenceToggleStore(
        store: DataStorePreferenceToggleStore
    ): PreferenceToggleStore

    @Binds
    abstract fun bindTagStore(repository: TagRepository): TagStore

    @Binds
    abstract fun bindTransactionStore(repository: TransactionRepository): TransactionStore
}
