package com.ivy.data.di

import com.ivy.data.DataWriteEventBus
import com.ivy.data.api.AccountStore
import com.ivy.data.api.AppPreferenceStore
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
import com.ivy.data.store.RoomAccountStore
import com.ivy.data.store.RoomBudgetStore
import com.ivy.data.store.RoomCategoryStore
import com.ivy.data.store.RoomCurrencyStore
import com.ivy.data.store.DefaultExchangeRateStore
import com.ivy.data.store.RoomLoanRecordStore
import com.ivy.data.store.RoomLoanStore
import com.ivy.data.store.RoomPlannedPaymentRuleStore
import com.ivy.data.store.RoomSettingsStore
import com.ivy.data.store.RoomTagStore
import com.ivy.data.store.RoomTransactionStore
import com.ivy.data.datastore.DataStorePreferenceToggleStore
import com.ivy.data.preferences.SharedPrefsAppPreferenceStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class StoreModule {
    @Binds
    abstract fun bindDataChangePublisher(eventBus: DataWriteEventBus): DataChangePublisher

    @Binds
    abstract fun bindAccountStore(store: RoomAccountStore): AccountStore

    @Binds
    abstract fun bindAppPreferenceStore(store: SharedPrefsAppPreferenceStore): AppPreferenceStore

    @Binds
    abstract fun bindBudgetStore(store: RoomBudgetStore): BudgetStore

    @Binds
    abstract fun bindCategoryStore(store: RoomCategoryStore): CategoryStore

    @Binds
    abstract fun bindCurrencyStore(store: RoomCurrencyStore): CurrencyStore

    @Binds
    abstract fun bindSettingsStore(store: RoomSettingsStore): SettingsStore

    @Binds
    abstract fun bindExchangeRateStore(store: DefaultExchangeRateStore): ExchangeRateStore

    @Binds
    abstract fun bindLoanStore(store: RoomLoanStore): LoanStore

    @Binds
    abstract fun bindLoanRecordStore(store: RoomLoanRecordStore): LoanRecordStore

    @Binds
    abstract fun bindPlannedPaymentRuleStore(
        store: RoomPlannedPaymentRuleStore
    ): PlannedPaymentRuleStore

    @Binds
    abstract fun bindPreferenceToggleStore(
        store: DataStorePreferenceToggleStore
    ): PreferenceToggleStore

    @Binds
    abstract fun bindTagStore(store: RoomTagStore): TagStore

    @Binds
    abstract fun bindTransactionStore(store: RoomTransactionStore): TransactionStore
}
