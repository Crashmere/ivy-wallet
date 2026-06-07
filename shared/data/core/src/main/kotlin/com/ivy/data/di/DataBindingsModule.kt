package com.ivy.data.di

import com.ivy.data.DataWriteEventBus
import com.ivy.data.api.AccountStore
import com.ivy.data.api.AppLockPreferenceStore
import com.ivy.data.api.AppPreferenceResetStore
import com.ivy.data.api.BackupSettingsPreferenceStore
import com.ivy.data.api.BalancePrivacyPreferenceStore
import com.ivy.data.api.BufferAmountStore
import com.ivy.data.api.backup.BackupStore
import com.ivy.data.api.BudgetStore
import com.ivy.data.api.CategoryStore
import com.ivy.data.api.CategorySortOrderStore
import com.ivy.data.api.CurrencyStore
import com.ivy.data.api.CustomerJourneyCardStore
import com.ivy.data.api.DataChangePublisher
import com.ivy.data.api.ExchangeRateStore
import com.ivy.data.api.file.TextFileStore
import com.ivy.data.api.InitialSetupStore
import com.ivy.data.api.LastSelectedAccountStore
import com.ivy.data.api.LoanRecordStore
import com.ivy.data.api.LoanStore
import com.ivy.data.api.NotificationPreferenceStore
import com.ivy.data.api.PlannedPaymentRuleStore
import com.ivy.data.api.PreferenceToggleStore
import com.ivy.data.api.SettingsInitializationStore
import com.ivy.data.api.SettingsResetStore
import com.ivy.data.api.StartDayOfMonthStore
import com.ivy.data.api.TagStore
import com.ivy.data.api.ThemeStore
import com.ivy.data.api.TransactionStore
import com.ivy.data.api.TransferBehaviorPreferenceStore
import com.ivy.data.backup.DefaultBackupStore
import com.ivy.data.datastore.DataStorePreferenceToggleStore
import com.ivy.data.file.FileSystem
import com.ivy.data.preferences.SharedPrefsAppPreferenceStore
import com.ivy.data.remote.RemoteExchangeRatesDataSource
import com.ivy.data.remote.impl.RemoteExchangeRatesDataSourceImpl
import com.ivy.data.store.DefaultExchangeRateStore
import com.ivy.data.store.RoomAccountStore
import com.ivy.data.store.RoomBudgetStore
import com.ivy.data.store.RoomCategoryStore
import com.ivy.data.store.RoomCurrencyStore
import com.ivy.data.store.RoomLoanRecordStore
import com.ivy.data.store.RoomLoanStore
import com.ivy.data.store.RoomPlannedPaymentRuleStore
import com.ivy.data.store.RoomSettingsStore
import com.ivy.data.store.RoomTagStore
import com.ivy.data.store.RoomTransactionStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindingsModule {
    @Binds
    abstract fun bindDataChangePublisher(eventBus: DataWriteEventBus): DataChangePublisher

    @Binds
    abstract fun bindAccountStore(store: RoomAccountStore): AccountStore

    @Binds
    abstract fun bindAppLockPreferenceStore(
        store: SharedPrefsAppPreferenceStore
    ): AppLockPreferenceStore

    @Binds
    abstract fun bindNotificationPreferenceStore(
        store: SharedPrefsAppPreferenceStore
    ): NotificationPreferenceStore

    @Binds
    abstract fun bindBalancePrivacyPreferenceStore(
        store: SharedPrefsAppPreferenceStore
    ): BalancePrivacyPreferenceStore

    @Binds
    abstract fun bindStartDayOfMonthStore(
        store: SharedPrefsAppPreferenceStore
    ): StartDayOfMonthStore

    @Binds
    abstract fun bindTransferBehaviorPreferenceStore(
        store: SharedPrefsAppPreferenceStore
    ): TransferBehaviorPreferenceStore

    @Binds
    abstract fun bindBackupSettingsPreferenceStore(
        store: SharedPrefsAppPreferenceStore
    ): BackupSettingsPreferenceStore

    @Binds
    abstract fun bindAppPreferenceResetStore(
        store: SharedPrefsAppPreferenceStore
    ): AppPreferenceResetStore

    @Binds
    abstract fun bindInitialSetupStore(store: SharedPrefsAppPreferenceStore): InitialSetupStore

    @Binds
    abstract fun bindLastSelectedAccountStore(
        store: SharedPrefsAppPreferenceStore
    ): LastSelectedAccountStore

    @Binds
    abstract fun bindCategorySortOrderStore(
        store: SharedPrefsAppPreferenceStore
    ): CategorySortOrderStore

    @Binds
    abstract fun bindCustomerJourneyCardStore(
        store: SharedPrefsAppPreferenceStore
    ): CustomerJourneyCardStore

    @Binds
    abstract fun bindBackupStore(defaultBackupStore: DefaultBackupStore): BackupStore

    @Binds
    abstract fun bindBudgetStore(store: RoomBudgetStore): BudgetStore

    @Binds
    abstract fun bindCategoryStore(store: RoomCategoryStore): CategoryStore

    @Binds
    abstract fun bindCurrencyStore(store: RoomCurrencyStore): CurrencyStore

    @Binds
    abstract fun bindExchangeRatesDataSource(
        dataSource: RemoteExchangeRatesDataSourceImpl
    ): RemoteExchangeRatesDataSource

    @Binds
    abstract fun bindTextFileStore(fileSystem: FileSystem): TextFileStore

    @Binds
    abstract fun bindSettingsInitializationStore(
        store: RoomSettingsStore
    ): SettingsInitializationStore

    @Binds
    abstract fun bindSettingsResetStore(store: RoomSettingsStore): SettingsResetStore

    @Binds
    abstract fun bindThemeStore(store: RoomSettingsStore): ThemeStore

    @Binds
    abstract fun bindBufferAmountStore(store: RoomSettingsStore): BufferAmountStore

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
