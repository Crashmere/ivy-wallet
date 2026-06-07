package com.ivy.data.preferences

import android.content.Context
import com.ivy.data.api.AppPreferenceKeys
import com.ivy.data.api.AppPreferenceStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SharedPrefsAppPreferenceStore @Inject constructor(
    @ApplicationContext
    context: Context
) : AppPreferenceStore {
    companion object {
        private const val PREFS_FILENAME = "ivy_wallet_prefs"
    }

    private val preferences = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)

    override var initialSetupCompleted: Boolean
        get() = preferences.getBoolean(AppPreferenceKeys.INITIAL_SETUP_COMPLETED, false)
        set(value) = preferences.edit().putBoolean(AppPreferenceKeys.INITIAL_SETUP_COMPLETED, value).apply()

    override var appLockEnabled: Boolean
        get() = preferences.getBoolean(AppPreferenceKeys.APP_LOCK_ENABLED, false)
        set(value) = preferences.edit().putBoolean(AppPreferenceKeys.APP_LOCK_ENABLED, value).apply()

    override var startDayOfMonth: Int
        get() = preferences.getInt(AppPreferenceKeys.START_DATE_OF_MONTH, 1)
        set(value) = preferences.edit().putInt(AppPreferenceKeys.START_DATE_OF_MONTH, value).apply()

    override var showNotifications: Boolean
        get() = preferences.getBoolean(AppPreferenceKeys.SHOW_NOTIFICATIONS, true)
        set(value) = preferences.edit().putBoolean(AppPreferenceKeys.SHOW_NOTIFICATIONS, value).apply()

    override var hideCurrentBalance: Boolean
        get() = preferences.getBoolean(AppPreferenceKeys.HIDE_CURRENT_BALANCE, false)
        set(value) = preferences.edit().putBoolean(AppPreferenceKeys.HIDE_CURRENT_BALANCE, value).apply()

    override var hideIncome: Boolean
        get() = preferences.getBoolean(AppPreferenceKeys.HIDE_INCOME, false)
        set(value) = preferences.edit().putBoolean(AppPreferenceKeys.HIDE_INCOME, value).apply()

    override var transfersAsIncomeExpense: Boolean
        get() = preferences.getBoolean(AppPreferenceKeys.TRANSFERS_AS_INCOME_EXPENSE, false)
        set(value) = preferences.edit().putBoolean(AppPreferenceKeys.TRANSFERS_AS_INCOME_EXPENSE, value).apply()

    override var categorySortOrder: Int
        get() = preferences.getInt(AppPreferenceKeys.CATEGORY_SORT_ORDER, 0)
        set(value) = preferences.edit().putInt(AppPreferenceKeys.CATEGORY_SORT_ORDER, value).apply()

    override var lastSelectedAccountId: String?
        get() = preferences.getString(AppPreferenceKeys.LAST_SELECTED_ACCOUNT_ID, null)
        set(value) = preferences.edit().putString(AppPreferenceKeys.LAST_SELECTED_ACCOUNT_ID, value).apply()

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
        return "$cardId${AppPreferenceKeys.CUSTOMER_JOURNEY_CARD_DISMISSED_SUFFIX}"
    }
}
