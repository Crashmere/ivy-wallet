package com.ivy.data.preferences

import android.content.Context
import com.ivy.data.api.AppLockPreferenceStore
import com.ivy.data.api.BackupSettingsPreferenceStore
import com.ivy.data.api.BalancePrivacyPreferenceStore
import com.ivy.data.api.CategorySortOrderStore
import com.ivy.data.api.CustomerJourneyCardStore
import com.ivy.data.api.InitialSetupStore
import com.ivy.data.api.LastSelectedAccountStore
import com.ivy.data.api.LocalPreferenceResetStore
import com.ivy.data.api.NotificationPreferenceStore
import com.ivy.data.api.StartDayOfMonthStore
import com.ivy.data.api.TransferBehaviorPreferenceStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class SharedPrefsPreferenceStore @Inject constructor(
    @ApplicationContext
    context: Context
) : AppLockPreferenceStore,
    NotificationPreferenceStore,
    BalancePrivacyPreferenceStore,
    StartDayOfMonthStore,
    TransferBehaviorPreferenceStore,
    BackupSettingsPreferenceStore,
    InitialSetupStore,
    LastSelectedAccountStore,
    CategorySortOrderStore,
    CustomerJourneyCardStore,
    LocalPreferenceResetStore {
    companion object {
        private const val PREFS_FILENAME = "ivy_wallet_prefs"
    }

    private val preferences = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)

    override var initialSetupCompleted: Boolean
        get() = preferences.getBoolean(SharedPreferenceKeys.INITIAL_SETUP_COMPLETED, false)
        set(value) = preferences.edit().putBoolean(SharedPreferenceKeys.INITIAL_SETUP_COMPLETED, value).apply()

    override var appLockEnabled: Boolean
        get() = preferences.getBoolean(SharedPreferenceKeys.APP_LOCK_ENABLED, false)
        set(value) = preferences.edit().putBoolean(SharedPreferenceKeys.APP_LOCK_ENABLED, value).apply()

    override var startDayOfMonth: Int
        get() = preferences.getInt(SharedPreferenceKeys.START_DATE_OF_MONTH, 1)
        set(value) = preferences.edit().putInt(SharedPreferenceKeys.START_DATE_OF_MONTH, value).apply()

    override var showNotifications: Boolean
        get() = preferences.getBoolean(SharedPreferenceKeys.SHOW_NOTIFICATIONS, true)
        set(value) = preferences.edit().putBoolean(SharedPreferenceKeys.SHOW_NOTIFICATIONS, value).apply()

    override var hideCurrentBalance: Boolean
        get() = preferences.getBoolean(SharedPreferenceKeys.HIDE_CURRENT_BALANCE, false)
        set(value) = preferences.edit().putBoolean(SharedPreferenceKeys.HIDE_CURRENT_BALANCE, value).apply()

    override var hideIncome: Boolean
        get() = preferences.getBoolean(SharedPreferenceKeys.HIDE_INCOME, false)
        set(value) = preferences.edit().putBoolean(SharedPreferenceKeys.HIDE_INCOME, value).apply()

    override var transfersAsIncomeExpense: Boolean
        get() = preferences.getBoolean(SharedPreferenceKeys.TRANSFERS_AS_INCOME_EXPENSE, false)
        set(value) = preferences.edit().putBoolean(SharedPreferenceKeys.TRANSFERS_AS_INCOME_EXPENSE, value).apply()

    override var categorySortOrder: Int
        get() = preferences.getInt(SharedPreferenceKeys.CATEGORY_SORT_ORDER, 0)
        set(value) = preferences.edit().putInt(SharedPreferenceKeys.CATEGORY_SORT_ORDER, value).apply()

    override var lastSelectedAccountId: String?
        get() = preferences.getString(SharedPreferenceKeys.LAST_SELECTED_ACCOUNT_ID, null)
        set(value) = preferences.edit().putString(SharedPreferenceKeys.LAST_SELECTED_ACCOUNT_ID, value).apply()

    override fun isCustomerJourneyCardDismissed(cardId: String): Boolean {
        return preferences.getBoolean(customerJourneyCardDismissedKey(cardId), false)
    }

    override fun dismissCustomerJourneyCard(cardId: String) {
        preferences.edit().putBoolean(customerJourneyCardDismissedKey(cardId), true).apply()
    }

    override fun clearAll() {
        preferences.edit().clear().apply()
    }

    private fun customerJourneyCardDismissedKey(cardId: String): String {
        return "$cardId${SharedPreferenceKeys.CUSTOMER_JOURNEY_CARD_DISMISSED_SUFFIX}"
    }
}
