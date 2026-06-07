package com.ivy.data.api

interface SettingsPreferenceStore {
    var appLockEnabled: Boolean

    var startDayOfMonth: Int

    var showNotifications: Boolean

    var hideCurrentBalance: Boolean

    var hideIncome: Boolean

    var transfersAsIncomeExpense: Boolean
}
