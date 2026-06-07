package com.ivy.data.preferences

import com.ivy.base.prefs.PreferenceStore
import com.ivy.data.api.AppPreferenceKeys
import com.ivy.data.api.AppPreferenceStore
import javax.inject.Inject

class SharedPrefsAppPreferenceStore @Inject constructor(
    private val preferenceStore: PreferenceStore
) : AppPreferenceStore {
    override var initialSetupCompleted: Boolean
        get() = preferenceStore.getBoolean(AppPreferenceKeys.INITIAL_SETUP_COMPLETED, false)
        set(value) = preferenceStore.putBoolean(AppPreferenceKeys.INITIAL_SETUP_COMPLETED, value)

    override var appLockEnabled: Boolean
        get() = preferenceStore.getBoolean(AppPreferenceKeys.APP_LOCK_ENABLED, false)
        set(value) = preferenceStore.putBoolean(AppPreferenceKeys.APP_LOCK_ENABLED, value)

    override var startDayOfMonth: Int
        get() = preferenceStore.getInt(AppPreferenceKeys.START_DATE_OF_MONTH, 1)
        set(value) = preferenceStore.putInt(AppPreferenceKeys.START_DATE_OF_MONTH, value)

    override var showNotifications: Boolean
        get() = preferenceStore.getBoolean(AppPreferenceKeys.SHOW_NOTIFICATIONS, true)
        set(value) = preferenceStore.putBoolean(AppPreferenceKeys.SHOW_NOTIFICATIONS, value)

    override var hideCurrentBalance: Boolean
        get() = preferenceStore.getBoolean(AppPreferenceKeys.HIDE_CURRENT_BALANCE, false)
        set(value) = preferenceStore.putBoolean(AppPreferenceKeys.HIDE_CURRENT_BALANCE, value)

    override var hideIncome: Boolean
        get() = preferenceStore.getBoolean(AppPreferenceKeys.HIDE_INCOME, false)
        set(value) = preferenceStore.putBoolean(AppPreferenceKeys.HIDE_INCOME, value)

    override var transfersAsIncomeExpense: Boolean
        get() = preferenceStore.getBoolean(AppPreferenceKeys.TRANSFERS_AS_INCOME_EXPENSE, false)
        set(value) = preferenceStore.putBoolean(AppPreferenceKeys.TRANSFERS_AS_INCOME_EXPENSE, value)

    override var categorySortOrder: Int
        get() = preferenceStore.getInt(AppPreferenceKeys.CATEGORY_SORT_ORDER, 0)
        set(value) = preferenceStore.putInt(AppPreferenceKeys.CATEGORY_SORT_ORDER, value)

    override var lastSelectedAccountId: String?
        get() = preferenceStore.getString(AppPreferenceKeys.LAST_SELECTED_ACCOUNT_ID, null)
        set(value) = preferenceStore.putString(AppPreferenceKeys.LAST_SELECTED_ACCOUNT_ID, value)

    override fun isCustomerJourneyCardDismissed(cardId: String): Boolean {
        return preferenceStore.getBoolean(customerJourneyCardDismissedKey(cardId), false)
    }

    override fun dismissCustomerJourneyCard(cardId: String) {
        preferenceStore.putBoolean(customerJourneyCardDismissedKey(cardId), true)
    }

    override fun clearAll() {
        preferenceStore.removeAll()
    }

    private fun customerJourneyCardDismissedKey(cardId: String): String {
        return "$cardId${AppPreferenceKeys.CUSTOMER_JOURNEY_CARD_DISMISSED_SUFFIX}"
    }
}
