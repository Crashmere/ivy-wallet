package com.ivy.settings

import com.ivy.data.model.GitHubBackupConfig

internal sealed interface SettingsEvent {
    data class SetCurrency(val newCurrency: String) : SettingsEvent
    data object ExportToCsv : SettingsEvent
    data object BackupData : SettingsEvent
    data class SaveGitHubBackupConfig(val config: GitHubBackupConfig) : SettingsEvent
    data object ClearGitHubBackupConfig : SettingsEvent
    data class TestGitHubConnection(val config: GitHubBackupConfig) : SettingsEvent
    data object BackupToGitHub : SettingsEvent
    data object RestoreFromGitHub : SettingsEvent
    data object SwitchTheme : SettingsEvent
    data class SetLockApp(val lockApp: Boolean) : SettingsEvent
    data class SetShowNotifications(val showNotifications: Boolean) : SettingsEvent
    data class SetHideCurrentBalance(val hideCurrentBalance: Boolean) : SettingsEvent
    data class SetHideIncome(val hideIncome: Boolean) : SettingsEvent
    data class SetTransfersAsIncomeExpense(val treatTransfersAsIncomeExpense: Boolean) :
        SettingsEvent

    data class SetCompactAccountsMode(val enabled: Boolean) : SettingsEvent
    data class SetHideAccountTotalBalance(val enabled: Boolean) : SettingsEvent
    data class SetCompactCategoriesMode(val enabled: Boolean) : SettingsEvent
    data class SetShowAccountColorsInTransactions(val enabled: Boolean) : SettingsEvent
    data class SetShowTitleSuggestions(val enabled: Boolean) : SettingsEvent
    data class SetStandardKeypadLayout(val enabled: Boolean) : SettingsEvent
    data class SetShowCategorySearchBar(val enabled: Boolean) : SettingsEvent
    data class SetSortCategoriesAscending(val enabled: Boolean) : SettingsEvent
    data class SetShowPlannedPaymentsQuickAccess(val enabled: Boolean) : SettingsEvent
    data class SetShowBudgetsQuickAccess(val enabled: Boolean) : SettingsEvent
    data class SetShowLoansQuickAccess(val enabled: Boolean) : SettingsEvent
    data class SetStartDateOfMonth(val startDate: Int) : SettingsEvent
    data object DeleteAllUserData : SettingsEvent
    data object SwitchLanguage : SettingsEvent
}
