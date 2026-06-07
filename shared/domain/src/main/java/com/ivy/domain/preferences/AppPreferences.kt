package com.ivy.domain.preferences

import com.ivy.base.prefs.PreferenceStore
import com.ivy.data.api.AppPreferenceKeys
import javax.inject.Inject

class AppPreferences @Inject constructor(
    private val preferenceStore: PreferenceStore
) {
    var initialSetupCompleted: Boolean
        get() = preferenceStore.getBoolean(AppPreferenceKeys.INITIAL_SETUP_COMPLETED, false)
        set(value) = preferenceStore.putBoolean(AppPreferenceKeys.INITIAL_SETUP_COMPLETED, value)

    var appLockEnabled: Boolean
        get() = preferenceStore.getBoolean(AppPreferenceKeys.APP_LOCK_ENABLED, false)
        set(value) = preferenceStore.putBoolean(AppPreferenceKeys.APP_LOCK_ENABLED, value)

    var startDayOfMonth: Int
        get() = preferenceStore.getInt(AppPreferenceKeys.START_DATE_OF_MONTH, 1)
        set(value) = preferenceStore.putInt(AppPreferenceKeys.START_DATE_OF_MONTH, value)

    var showNotifications: Boolean
        get() = preferenceStore.getBoolean(AppPreferenceKeys.SHOW_NOTIFICATIONS, true)
        set(value) = preferenceStore.putBoolean(AppPreferenceKeys.SHOW_NOTIFICATIONS, value)

    var hideCurrentBalance: Boolean
        get() = preferenceStore.getBoolean(AppPreferenceKeys.HIDE_CURRENT_BALANCE, false)
        set(value) = preferenceStore.putBoolean(AppPreferenceKeys.HIDE_CURRENT_BALANCE, value)

    var hideIncome: Boolean
        get() = preferenceStore.getBoolean(AppPreferenceKeys.HIDE_INCOME, false)
        set(value) = preferenceStore.putBoolean(AppPreferenceKeys.HIDE_INCOME, value)

    var transfersAsIncomeExpense: Boolean
        get() = preferenceStore.getBoolean(AppPreferenceKeys.TRANSFERS_AS_INCOME_EXPENSE, false)
        set(value) = preferenceStore.putBoolean(AppPreferenceKeys.TRANSFERS_AS_INCOME_EXPENSE, value)

    var categorySortOrder: Int
        get() = preferenceStore.getInt(AppPreferenceKeys.CATEGORY_SORT_ORDER, 0)
        set(value) = preferenceStore.putInt(AppPreferenceKeys.CATEGORY_SORT_ORDER, value)

    var lastSelectedAccountId: String?
        get() = preferenceStore.getString(AppPreferenceKeys.LAST_SELECTED_ACCOUNT_ID, null)
        set(value) = preferenceStore.putString(AppPreferenceKeys.LAST_SELECTED_ACCOUNT_ID, value)

    fun isCustomerJourneyCardDismissed(cardId: String): Boolean {
        return preferenceStore.getBoolean(customerJourneyCardDismissedKey(cardId), false)
    }

    fun dismissCustomerJourneyCard(cardId: String) {
        preferenceStore.putBoolean(customerJourneyCardDismissedKey(cardId), true)
    }

    fun clearAll() {
        preferenceStore.removeAll()
    }

    private fun customerJourneyCardDismissedKey(cardId: String): String {
        return "$cardId${AppPreferenceKeys.CUSTOMER_JOURNEY_CARD_DISMISSED_SUFFIX}"
    }
}
