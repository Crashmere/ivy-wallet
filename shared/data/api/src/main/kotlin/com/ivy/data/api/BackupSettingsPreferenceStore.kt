package com.ivy.data.api

interface BackupSettingsPreferenceStore {
    var showNotifications: Boolean

    var appLockEnabled: Boolean

    var hideCurrentBalance: Boolean

    var transfersAsIncomeExpense: Boolean
}
