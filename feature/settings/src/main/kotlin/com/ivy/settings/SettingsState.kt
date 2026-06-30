package com.ivy.settings

import com.ivy.data.model.GitHubBackupConfig
import com.ivy.data.model.Theme

internal data class SettingsState(
    val currencyCode: String,
    val currentTheme: Theme,
    val gitHubBackupConfig: GitHubBackupConfig?,
    val gitHubLastBackupEpochSec: Long?,
    val lockApp: Boolean,
    val showNotifications: Boolean,
    val hideCurrentBalance: Boolean,
    val hideIncome: Boolean,
    val treatTransfersAsIncomeExpense: Boolean,
    val compactAccountsMode: Boolean,
    val hideAccountTotalBalance: Boolean,
    val compactCategoriesMode: Boolean,
    val showAccountColorsInTransactions: Boolean,
    val showTitleSuggestions: Boolean,
    val standardKeypadLayout: Boolean,
    val showCategorySearchBar: Boolean,
    val sortCategoriesAscending: Boolean,
    val startDateOfMonth: String,
    val progressState: Boolean,
    val languageOptionVisible: Boolean
)
