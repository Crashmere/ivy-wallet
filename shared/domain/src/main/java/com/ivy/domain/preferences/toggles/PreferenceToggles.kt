package com.ivy.domain.preferences.toggles

interface PreferenceToggles {
    val sortCategoriesAscending: BoolPreference
    val compactAccountsMode: BoolPreference
    val compactCategoriesMode: BoolPreference
    val showTitleSuggestions: BoolPreference
    val showCategorySearchBar: BoolPreference
    val hideTotalBalance: BoolPreference
    val showDecimalNumber: BoolPreference
    val standardKeypadLayout: BoolPreference
    val showAccountColorsInTransactions: BoolPreference

    val allPreferences: List<BoolPreference>
}
