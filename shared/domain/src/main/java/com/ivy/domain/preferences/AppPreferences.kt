package com.ivy.domain.preferences

import com.ivy.base.prefs.SharedPrefs
import javax.inject.Inject

class AppPreferences @Inject constructor(
    private val sharedPrefs: SharedPrefs
) {
    var initialSetupCompleted: Boolean
        get() = sharedPrefs.getBoolean(SharedPrefs.INITIAL_SETUP_COMPLETED, false)
        set(value) = sharedPrefs.putBoolean(SharedPrefs.INITIAL_SETUP_COMPLETED, value)

    var appLockEnabled: Boolean
        get() = sharedPrefs.getBoolean(SharedPrefs.APP_LOCK_ENABLED, false)
        set(value) = sharedPrefs.putBoolean(SharedPrefs.APP_LOCK_ENABLED, value)

    var startDayOfMonth: Int
        get() = sharedPrefs.getInt(SharedPrefs.START_DATE_OF_MONTH, 1)
        set(value) = sharedPrefs.putInt(SharedPrefs.START_DATE_OF_MONTH, value)

    var showNotifications: Boolean
        get() = sharedPrefs.getBoolean(SharedPrefs.SHOW_NOTIFICATIONS, true)
        set(value) = sharedPrefs.putBoolean(SharedPrefs.SHOW_NOTIFICATIONS, value)

    var hideCurrentBalance: Boolean
        get() = sharedPrefs.getBoolean(SharedPrefs.HIDE_CURRENT_BALANCE, false)
        set(value) = sharedPrefs.putBoolean(SharedPrefs.HIDE_CURRENT_BALANCE, value)

    var hideIncome: Boolean
        get() = sharedPrefs.getBoolean(SharedPrefs.HIDE_INCOME, false)
        set(value) = sharedPrefs.putBoolean(SharedPrefs.HIDE_INCOME, value)

    var transfersAsIncomeExpense: Boolean
        get() = sharedPrefs.getBoolean(SharedPrefs.TRANSFERS_AS_INCOME_EXPENSE, false)
        set(value) = sharedPrefs.putBoolean(SharedPrefs.TRANSFERS_AS_INCOME_EXPENSE, value)

    var dataBackupCompleted: Boolean
        get() = sharedPrefs.getBoolean(SharedPrefs.DATA_BACKUP_COMPLETED, false)
        set(value) = sharedPrefs.putBoolean(SharedPrefs.DATA_BACKUP_COMPLETED, value)

    var categorySortOrder: Int
        get() = sharedPrefs.getInt(SharedPrefs.CATEGORY_SORT_ORDER, 0)
        set(value) = sharedPrefs.putInt(SharedPrefs.CATEGORY_SORT_ORDER, value)

    var lastSelectedAccountId: String?
        get() = sharedPrefs.getString(SharedPrefs.LAST_SELECTED_ACCOUNT_ID, null)
        set(value) = sharedPrefs.putString(SharedPrefs.LAST_SELECTED_ACCOUNT_ID, value)

    fun isCustomerJourneyCardDismissed(cardId: String): Boolean {
        return sharedPrefs.getBoolean(customerJourneyCardDismissedKey(cardId), false)
    }

    fun dismissCustomerJourneyCard(cardId: String) {
        sharedPrefs.putBoolean(customerJourneyCardDismissedKey(cardId), true)
    }

    fun clearAll() {
        sharedPrefs.removeAll()
    }

    private fun customerJourneyCardDismissedKey(cardId: String): String {
        return "$cardId${SharedPrefs._CARD_DISMISSED}"
    }
}
