package com.ivy.domain.preferences.toggles

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceToggleCatalog @Inject constructor() {

    val sortCategoriesAscending = BoolPreference(
        key = "sort_categories_ascending",
        group = PreferenceGroup.Category,
        name = "Sort categories list",
        description = "Show categories in ascending order (A-Z) on the transaction entry screen",
        defaultValue = false
    )

    val compactAccountsMode = BoolPreference(
        key = "compact_account_ui",
        group = PreferenceGroup.Account,
        name = "Compact account cards",
        description = "Make the Accounts tab UI more compact and dense",
        defaultValue = false
    )

    val compactCategoriesMode = BoolPreference(
        key = "compact_category_ui",
        group = PreferenceGroup.Category,
        name = "Compact category cards",
        description = "Simplified design of the Categories screen",
        defaultValue = false
    )

    val showTitleSuggestions = BoolPreference(
        key = "show_title_suggestions",
        group = PreferenceGroup.Other,
        name = "Show previous title suggestions",
        description = "Suggest past transaction titles when creating a new entry",
        defaultValue = true
    )

    val showCategorySearchBar = BoolPreference(
        key = "search_categories",
        group = PreferenceGroup.Category,
        name = "Search within categories",
        description = "Display a search bar on the Categories screen",
        defaultValue = true
    )

    val hideTotalBalance = BoolPreference(
        key = "hide_total_balance",
        group = PreferenceGroup.Account,
        name = "Hide account total balance",
        description = "Hide total balance summary on the Accounts screen",
        defaultValue = false
    )

    val showDecimalNumber = BoolPreference(
        key = "show_decimal_number",
        group = PreferenceGroup.Other,
        name = "Show values with decimals",
        description = "Include the decimal part in amounts",
        defaultValue = true
    )

    val standardKeypadLayout = BoolPreference(
        key = "enable_standard_keypad_layout",
        group = PreferenceGroup.Other,
        name = "Standard keypad layout",
        description = "Replace numeric keypad with standard phone layout",
        defaultValue = false
    )

    val showAccountColorsInTransactions = BoolPreference(
        key = "show_account_color",
        group = PreferenceGroup.Other,
        name = "Colorful account labels",
        description = "Display account-specific colors in transactions",
        defaultValue = false
    )

    val allPreferences: List<BoolPreference>
        get() = listOf(
            sortCategoriesAscending,
            compactAccountsMode,
            compactCategoriesMode,
            showTitleSuggestions,
            showCategorySearchBar,
            hideTotalBalance,
            standardKeypadLayout,
            showAccountColorsInTransactions
            /* will be uncommented when this functionality
             * will be available across the application in up-coming PRs
            showDecimalNumber
             */
        )
}
