package com.ivy.data.preferences

internal object SharedPreferenceKeys {
    const val INITIAL_SETUP_COMPLETED = "onboarding_completed"

    const val LAST_SELECTED_ACCOUNT_ID = "last_selected_account_id"

    const val APP_LOCK_ENABLED = "lock_app"
    const val START_DATE_OF_MONTH = "start_date_of_month"
    const val SHOW_NOTIFICATIONS = "show_notifications"
    const val HIDE_CURRENT_BALANCE = "hide_current_balance"
    const val HIDE_INCOME = "hide_income"
    const val TRANSFERS_AS_INCOME_EXPENSE = "transfers_as_inc_exp"

    const val CUSTOMER_JOURNEY_CARD_DISMISSED_SUFFIX = "_cj_dismissed"

    const val CATEGORY_SORT_ORDER = "categorySortOrder"

    const val GITHUB_BACKUP_TOKEN = "github_backup_token"
    const val GITHUB_BACKUP_OWNER = "github_backup_owner"
    const val GITHUB_BACKUP_REPO = "github_backup_repo"
    const val GITHUB_BACKUP_BRANCH = "github_backup_branch"
    const val GITHUB_BACKUP_PATH = "github_backup_path"
    const val GITHUB_BACKUP_LAST_EPOCH_SEC = "github_backup_last_epoch_sec"
}
