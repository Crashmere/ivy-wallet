package com.ivy.data.api

interface AppPreferenceStore {
    var appLockEnabled: Boolean

    var startDayOfMonth: Int

    var showNotifications: Boolean

    var hideCurrentBalance: Boolean

    var hideIncome: Boolean

    var transfersAsIncomeExpense: Boolean

    var categorySortOrder: Int

    var lastSelectedAccountId: String?

    fun isCustomerJourneyCardDismissed(cardId: String): Boolean

    fun dismissCustomerJourneyCard(cardId: String)

    fun clearAll()
}
