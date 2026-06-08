package com.ivy.settings

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.data.model.Theme
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.legacy.ui.theme.style
import com.ivy.ui.compose.thenIf
import com.ivy.ui.compose.drawColoredShadow
import com.ivy.ui.navigation.ExchangeRatesScreen
import com.ivy.ui.navigation.ImportScreen
import com.ivy.ui.navigation.MainScreen
import com.ivy.ui.navigation.navigation
import com.ivy.ui.navigation.screenScopedViewModel
import com.ivy.ui.platform.buildInfoProvider
import com.ivy.ui.platform.fileSharer
import com.ivy.ui.R
import com.ivy.data.model.currency.IvyCurrency
import com.ivy.ui.theme.colors.Gradient
import com.ivy.ui.theme.colors.IvyGradients
import com.ivy.ui.theme.colors.IvyFixedColors.White
import com.ivy.legacy.ui.modal.CurrencyModal
import com.ivy.legacy.ui.modal.DeleteModal
import com.ivy.legacy.ui.modal.ProgressModal
import java.util.Locale

private enum class SettingsPage(@StringRes val title: Int) {
    Main(R.string.settings),
    DisplayPreferences(R.string.display_preferences),
    InputAndLists(R.string.input_and_lists)
}

@ExperimentalFoundationApi
@Composable
fun BoxWithConstraintsScope.SettingsScreen() {
    val viewModel: SettingsViewModel = screenScopedViewModel()
    val uiState = viewModel.uiState()
    val fileSharer = fileSharer()
    val nav = navigation()

    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                SettingsUiEvent.WalletDataReset -> {
                    nav.resetBackStack()
                    nav.navigateTo(MainScreen)
                }

                is SettingsUiEvent.ShareCsvFile -> fileSharer.shareCSVFile(event.fileUri)
                is SettingsUiEvent.ShareZipFile -> fileSharer.shareZipFile(event.fileUri)
            }
        }
    }

    UI(
        currencyCode = uiState.currencyCode,
        theme = uiState.currentTheme,
        onSwitchTheme = {
            viewModel.onEvent(SettingsEvent.SwitchTheme)
        },
        lockApp = uiState.lockApp,
        showNotifications = uiState.showNotifications,
        hideCurrentBalance = uiState.hideCurrentBalance,
        hideIncome = uiState.hideIncome,
        progressState = uiState.progressState,
        treatTransfersAsIncomeExpense = uiState.treatTransfersAsIncomeExpense,
        compactAccountsMode = uiState.compactAccountsMode,
        hideAccountTotalBalance = uiState.hideAccountTotalBalance,
        compactCategoriesMode = uiState.compactCategoriesMode,
        showAccountColorsInTransactions = uiState.showAccountColorsInTransactions,
        showTitleSuggestions = uiState.showTitleSuggestions,
        standardKeypadLayout = uiState.standardKeypadLayout,
        showCategorySearchBar = uiState.showCategorySearchBar,
        sortCategoriesAscending = uiState.sortCategoriesAscending,
        startDateOfMonth = uiState.startDateOfMonth.toInt(),
        languageOptionVisible = uiState.languageOptionVisible,
        onSetCurrency = {
            viewModel.onEvent(SettingsEvent.SetCurrency(it))
        },
        onBackupData = {
            viewModel.onEvent(SettingsEvent.BackupData)
        },
        onExportToCSV = {
            viewModel.onEvent(SettingsEvent.ExportToCsv)
        },
        onSetLockApp = {
            viewModel.onEvent(SettingsEvent.SetLockApp(it))
        },
        onSetShowNotifications = {
            viewModel.onEvent(SettingsEvent.SetShowNotifications(it))
        },
        onSetHideCurrentBalance = {
            viewModel.onEvent(SettingsEvent.SetHideCurrentBalance(it))
        },
        onSetHideIncome = {
            viewModel.onEvent(SettingsEvent.SetHideIncome(it))
        },
        onSetStartDateOfMonth = {
            viewModel.onEvent(SettingsEvent.SetStartDateOfMonth(it))
        },
        onSetTreatTransfersAsIncExp = {
            viewModel.onEvent(SettingsEvent.SetTransfersAsIncomeExpense(it))
        },
        onSetCompactAccountsMode = {
            viewModel.onEvent(SettingsEvent.SetCompactAccountsMode(it))
        },
        onSetHideAccountTotalBalance = {
            viewModel.onEvent(SettingsEvent.SetHideAccountTotalBalance(it))
        },
        onSetCompactCategoriesMode = {
            viewModel.onEvent(SettingsEvent.SetCompactCategoriesMode(it))
        },
        onSetShowAccountColorsInTransactions = {
            viewModel.onEvent(SettingsEvent.SetShowAccountColorsInTransactions(it))
        },
        onSetShowTitleSuggestions = {
            viewModel.onEvent(SettingsEvent.SetShowTitleSuggestions(it))
        },
        onSetStandardKeypadLayout = {
            viewModel.onEvent(SettingsEvent.SetStandardKeypadLayout(it))
        },
        onSetShowCategorySearchBar = {
            viewModel.onEvent(SettingsEvent.SetShowCategorySearchBar(it))
        },
        onSetSortCategoriesAscending = {
            viewModel.onEvent(SettingsEvent.SetSortCategoriesAscending(it))
        },
        onDeleteAllUserData = {
            viewModel.onEvent(SettingsEvent.DeleteAllUserData)
        },
        onSwitchLanguage = {
            viewModel.onEvent(SettingsEvent.SwitchLanguage)
        },
        onBack = nav::back,
        onOpenImport = {
            nav.navigateTo(ImportScreen)
        },
        onOpenExchangeRates = {
            nav.navigateTo(ExchangeRatesScreen)
        },
    )
}

@ExperimentalFoundationApi
@Composable
@Suppress("LongMethod")
private fun BoxWithConstraintsScope.UI(
    currencyCode: String,
    theme: Theme,
    onSwitchTheme: () -> Unit,
    lockApp: Boolean,
    languageOptionVisible: Boolean,
    onSetCurrency: (String) -> Unit,
    startDateOfMonth: Int = 1,
    showNotifications: Boolean = true,
    hideCurrentBalance: Boolean = false,
    hideIncome: Boolean = false,
    progressState: Boolean = false,
    treatTransfersAsIncomeExpense: Boolean = false,
    compactAccountsMode: Boolean = false,
    hideAccountTotalBalance: Boolean = false,
    compactCategoriesMode: Boolean = false,
    showAccountColorsInTransactions: Boolean = false,
    showTitleSuggestions: Boolean = true,
    standardKeypadLayout: Boolean = false,
    showCategorySearchBar: Boolean = true,
    sortCategoriesAscending: Boolean = false,
    onBackupData: () -> Unit = {},
    onExportToCSV: () -> Unit = {},
    onSetLockApp: (Boolean) -> Unit = {},
    onSetShowNotifications: (Boolean) -> Unit = {},
    onSetTreatTransfersAsIncExp: (Boolean) -> Unit = {},
    onSetHideCurrentBalance: (Boolean) -> Unit = {},
    onSetHideIncome: (Boolean) -> Unit = {},
    onSetCompactAccountsMode: (Boolean) -> Unit = {},
    onSetHideAccountTotalBalance: (Boolean) -> Unit = {},
    onSetCompactCategoriesMode: (Boolean) -> Unit = {},
    onSetShowAccountColorsInTransactions: (Boolean) -> Unit = {},
    onSetShowTitleSuggestions: (Boolean) -> Unit = {},
    onSetStandardKeypadLayout: (Boolean) -> Unit = {},
    onSetShowCategorySearchBar: (Boolean) -> Unit = {},
    onSetSortCategoriesAscending: (Boolean) -> Unit = {},
    onSetStartDateOfMonth: (Int) -> Unit = {},
    onDeleteAllUserData: () -> Unit = {},
    onSwitchLanguage: () -> Unit = {},
    onBack: () -> Unit = {},
    onOpenImport: () -> Unit = {},
    onOpenExchangeRates: () -> Unit = {},
) {
    var currencyModalVisible by remember { mutableStateOf(false) }
    var chooseStartDateOfMonthVisible by remember { mutableStateOf(false) }
    var deleteAllDataModalVisible by remember { mutableStateOf(false) }
    var deleteAllDataModalFinalVisible by remember { mutableStateOf(false) }
    var settingsPage by remember { mutableStateOf(SettingsPage.Main) }
    val mainListState = rememberLazyListState()
    val displayPreferencesListState = rememberLazyListState()
    val inputAndListsListState = rememberLazyListState()
    val currentListState = when (settingsPage) {
        SettingsPage.Main -> mainListState
        SettingsPage.DisplayPreferences -> displayPreferencesListState
        SettingsPage.InputAndLists -> inputAndListsListState
    }
    BackHandler(enabled = settingsPage != SettingsPage.Main) {
        settingsPage = SettingsPage.Main
    }

    LazyColumn(
        state = currentListState,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("settings_lazy_column")
    ) {
        stickyHeader {
            SettingsToolbarFrame(
                onBack = {
                    if (settingsPage == SettingsPage.Main) {
                        onBack()
                    } else {
                        settingsPage = SettingsPage.Main
                    }
                },
            ) {
                Spacer(Modifier.weight(1f))

                val buildInfoProvider = buildInfoProvider()
                Text(
                    modifier = Modifier,
                    text = "${buildInfoProvider.buildVersionName} (${buildInfoProvider.buildVersionCode})",
                    style = LegacyTheme.typo.nC.style(
                        color = LegacyTheme.colors.gray,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(Modifier.width(32.dp))
            }
        }

        item {
            Spacer(Modifier.height(8.dp))

            Text(
                modifier = Modifier.padding(start = 32.dp),
                text = stringResource(settingsPage.title),
                style = LegacyTheme.typo.h2.style(
                    fontWeight = FontWeight.Black
                )
            )

            Spacer(Modifier.height(24.dp))
        }

        when (settingsPage) {
            SettingsPage.Main -> {
                item {
                    DataManagementSection(
                        onExportToCSV = onExportToCSV,
                        onBackupData = onBackupData,
                        onImportData = onOpenImport
                    )
                }

                item {
                    AccountingRulesSection(
                        currencyCode = currencyCode,
                        startDateOfMonth = startDateOfMonth,
                        treatTransfersAsIncomeExpense = treatTransfersAsIncomeExpense,
                        onSetTreatTransfersAsIncExp = onSetTreatTransfersAsIncExp,
                        onCurrencyClick = {
                            currencyModalVisible = true
                        },
                        onStartDateClick = {
                            chooseStartDateOfMonthVisible = true
                        }
                    )
                }

                item {
                    SystemBehaviorSection(
                        lockApp = lockApp,
                        showNotifications = showNotifications,
                        languageOptionVisible = languageOptionVisible,
                        onSetLockApp = onSetLockApp,
                        onSetShowNotifications = onSetShowNotifications,
                        onSwitchLanguage = onSwitchLanguage,
                        onExchangeRatesClick = onOpenExchangeRates
                    )
                }

                item {
                    Spacer(Modifier.height(32.dp))

                    SettingsSubMenuButton(
                        icon = R.drawable.ic_custom_palette_m,
                        text = stringResource(R.string.display_preferences)
                    ) {
                        settingsPage = SettingsPage.DisplayPreferences
                    }

                    Spacer(Modifier.height(12.dp))

                    SettingsSubMenuButton(
                        icon = R.drawable.ic_custom_document_m,
                        text = stringResource(R.string.input_and_lists)
                    ) {
                        settingsPage = SettingsPage.InputAndLists
                    }
                }

                item {
                    DangerZoneSection {
                        deleteAllDataModalVisible = true
                    }
                }
            }

            SettingsPage.DisplayPreferences -> {
                item {
                    DisplayPreferencesSection(
                        theme = theme,
                        hideCurrentBalance = hideCurrentBalance,
                        hideIncome = hideIncome,
                        compactAccountsMode = compactAccountsMode,
                        hideAccountTotalBalance = hideAccountTotalBalance,
                        compactCategoriesMode = compactCategoriesMode,
                        showAccountColorsInTransactions = showAccountColorsInTransactions,
                        onSwitchTheme = onSwitchTheme,
                        onSetHideCurrentBalance = onSetHideCurrentBalance,
                        onSetHideIncome = onSetHideIncome,
                        onSetCompactAccountsMode = onSetCompactAccountsMode,
                        onSetHideAccountTotalBalance = onSetHideAccountTotalBalance,
                        onSetCompactCategoriesMode = onSetCompactCategoriesMode,
                        onSetShowAccountColorsInTransactions = onSetShowAccountColorsInTransactions
                    )
                }
            }

            SettingsPage.InputAndLists -> {
                item {
                    InputAndListsSection(
                        showTitleSuggestions = showTitleSuggestions,
                        standardKeypadLayout = standardKeypadLayout,
                        showCategorySearchBar = showCategorySearchBar,
                        sortCategoriesAscending = sortCategoriesAscending,
                        onSetShowTitleSuggestions = onSetShowTitleSuggestions,
                        onSetStandardKeypadLayout = onSetStandardKeypadLayout,
                        onSetShowCategorySearchBar = onSetShowCategorySearchBar,
                        onSetSortCategoriesAscending = onSetSortCategoriesAscending
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(120.dp)) // last item spacer
        }
    }

    CurrencyModal(
        title = stringResource(R.string.set_currency),
        initialCurrency = IvyCurrency.fromCode(currencyCode),
        visible = currencyModalVisible,
        dismiss = { currencyModalVisible = false }
    ) {
        onSetCurrency(it)
    }

    SettingsStartDateOfMonthModal(
        visible = chooseStartDateOfMonthVisible,
        selectedStartDateOfMonth = startDateOfMonth,
        dismiss = { chooseStartDateOfMonthVisible = false }
    ) {
        onSetStartDateOfMonth(it)
    }

    DeleteModal(
        title = stringResource(R.string.delete_all_user_data_question),
        description = stringResource(
            R.string.delete_all_user_data_warning,
            stringResource(R.string.your_account)
        ),
        visible = deleteAllDataModalVisible,
        dismiss = { deleteAllDataModalVisible = false },
        onDelete = {
            deleteAllDataModalVisible = false
            deleteAllDataModalFinalVisible = true
        }
    )

    DeleteModal(
        title = stringResource(
            R.string.confirm_all_userd_data_deletion,
            stringResource(R.string.all_of_your_data)
        ),
        description = stringResource(R.string.final_deletion_warning),
        visible = deleteAllDataModalFinalVisible,
        dismiss = { deleteAllDataModalFinalVisible = false },
        onDelete = {
            onDeleteAllUserData()
        }
    )

    ProgressModal(
        title = stringResource(R.string.exporting_data),
        description = stringResource(R.string.exporting_data_description),
        visible = progressState
    )
}

@Composable
private fun SettingsToolbarFrame(
    onBack: () -> Unit,
    content: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LegacyTheme.colors.pure)
            .padding(top = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(20.dp))

        Icon(
            modifier = Modifier
                .testTag("toolbar_back")
                .clip(CircleShape)
                .background(LegacyTheme.colors.pure, CircleShape)
                .border(2.dp, LegacyTheme.colors.medium, CircleShape)
                .clickable(onClick = onBack)
                .padding(6.dp),
            painter = painterResource(id = R.drawable.ic_back),
            contentDescription = "back",
            tint = LegacyTheme.colors.pureInverse,
        )

        content()
    }
}

@Composable
private fun DataManagementSection(
    onExportToCSV: () -> Unit,
    onBackupData: () -> Unit,
    onImportData: () -> Unit
) {
    SettingsSectionDivider(text = stringResource(R.string.data_management))

    Spacer(Modifier.height(16.dp))

    ExportCSV {
        onExportToCSV()
    }

    Spacer(Modifier.height(12.dp))

    SettingsDefaultButton(
        icon = R.drawable.ic_vue_security_shield,
        text = stringResource(R.string.backup_data),
        iconPadding = 8.dp
    ) {
        onBackupData()
    }

    Spacer(Modifier.height(12.dp))

    SettingsPrimaryButton(
        icon = R.drawable.ic_export_csv,
        text = stringResource(R.string.import_data),
        backgroundGradient = IvyGradients.Green
    ) {
        onImportData()
    }
}

@Composable
private fun AccountingRulesSection(
    currencyCode: String,
    startDateOfMonth: Int,
    treatTransfersAsIncomeExpense: Boolean,
    onSetTreatTransfersAsIncExp: (Boolean) -> Unit,
    onCurrencyClick: () -> Unit,
    onStartDateClick: () -> Unit
) {
    SettingsSectionDivider(text = stringResource(R.string.accounting_rules))

    Spacer(Modifier.height(16.dp))

    CurrencyButton(currency = currencyCode) {
        onCurrencyClick()
    }

    Spacer(Modifier.height(12.dp))

    StartDateOfMonth(
        startDateOfMonth = startDateOfMonth
    ) {
        onStartDateClick()
    }

    Spacer(Modifier.height(12.dp))

    AppSwitch(
        lockApp = treatTransfersAsIncomeExpense,
        onSetLockApp = onSetTreatTransfersAsIncExp,
        text = stringResource(R.string.transfers_as_income_expense),
        description = stringResource(R.string.transfers_as_income_expense_description),
        icon = R.drawable.ic_custom_transfer_m
    )
}

@Composable
private fun SystemBehaviorSection(
    lockApp: Boolean,
    showNotifications: Boolean,
    languageOptionVisible: Boolean,
    onSetLockApp: (Boolean) -> Unit,
    onSetShowNotifications: (Boolean) -> Unit,
    onSwitchLanguage: () -> Unit,
    onExchangeRatesClick: () -> Unit
) {
    SettingsSectionDivider(text = stringResource(R.string.system_behavior))

    Spacer(Modifier.height(16.dp))

    AppSwitch(
        lockApp = lockApp,
        onSetLockApp = onSetLockApp,
        text = stringResource(R.string.lock_app),
        icon = R.drawable.ic_custom_fingerprint_m
    )

    Spacer(Modifier.height(12.dp))

    AppSwitch(
        lockApp = showNotifications,
        onSetLockApp = onSetShowNotifications,
        text = stringResource(R.string.show_notifications),
        icon = R.drawable.ic_notification_m
    )

    Spacer(Modifier.height(12.dp))

    if (languageOptionVisible) {
        SettingsDefaultButton(
            icon = R.drawable.ic_vue_location_global,
            iconPadding = 6.dp,
            text = stringResource(R.string.language),
            description = Locale.getDefault().displayName
        ) {
            onSwitchLanguage()
        }

        Spacer(Modifier.height(12.dp))
    }

    SettingsDefaultButton(
        icon = R.drawable.ic_currency,
        text = stringResource(R.string.exchange_rates),
    ) {
        onExchangeRatesClick()
    }
}

@Composable
private fun DisplayPreferencesSection(
    theme: Theme,
    hideCurrentBalance: Boolean,
    hideIncome: Boolean,
    compactAccountsMode: Boolean,
    hideAccountTotalBalance: Boolean,
    compactCategoriesMode: Boolean,
    showAccountColorsInTransactions: Boolean,
    onSwitchTheme: () -> Unit,
    onSetHideCurrentBalance: (Boolean) -> Unit,
    onSetHideIncome: (Boolean) -> Unit,
    onSetCompactAccountsMode: (Boolean) -> Unit,
    onSetHideAccountTotalBalance: (Boolean) -> Unit,
    onSetCompactCategoriesMode: (Boolean) -> Unit,
    onSetShowAccountColorsInTransactions: (Boolean) -> Unit
) {
    AppThemeButton(
        icon = when (theme) {
            Theme.LIGHT -> R.drawable.home_more_menu_light_mode
            Theme.DARK -> R.drawable.home_more_menu_dark_mode
            Theme.AMOLED_DARK -> R.drawable.home_more_menu_amoled_dark_mode
            Theme.AUTO -> R.drawable.home_more_menu_auto_mode
        },
        label = when (theme) {
            Theme.LIGHT -> stringResource(R.string.light_mode)
            Theme.DARK -> stringResource(R.string.dark_mode)
            Theme.AMOLED_DARK -> stringResource(R.string.amoled_mode)
            Theme.AUTO -> stringResource(R.string.auto_mode)
        }
    ) {
        onSwitchTheme()
    }

    Spacer(Modifier.height(12.dp))

    AppSwitch(
        lockApp = hideCurrentBalance,
        onSetLockApp = onSetHideCurrentBalance,
        text = stringResource(R.string.hide_balance),
        description = stringResource(R.string.hide_balance_description),
        icon = R.drawable.ic_hide_m
    )

    Spacer(Modifier.height(12.dp))

    AppSwitch(
        lockApp = hideIncome,
        onSetLockApp = onSetHideIncome,
        text = stringResource(R.string.hide_income),
        description = stringResource(R.string.hide_income_description),
        icon = R.drawable.ic_hide_m
    )

    Spacer(Modifier.height(12.dp))

    AppSwitch(
        lockApp = compactAccountsMode,
        onSetLockApp = onSetCompactAccountsMode,
        text = stringResource(R.string.compact_account_cards),
        description = stringResource(R.string.compact_account_cards_description),
        icon = R.drawable.ic_custom_account_m
    )

    Spacer(Modifier.height(12.dp))

    AppSwitch(
        lockApp = hideAccountTotalBalance,
        onSetLockApp = onSetHideAccountTotalBalance,
        text = stringResource(R.string.hide_account_total_balance),
        description = stringResource(R.string.hide_account_total_balance_description),
        icon = R.drawable.ic_hide_m
    )

    Spacer(Modifier.height(12.dp))

    AppSwitch(
        lockApp = compactCategoriesMode,
        onSetLockApp = onSetCompactCategoriesMode,
        text = stringResource(R.string.compact_category_cards),
        description = stringResource(R.string.compact_category_cards_description),
        icon = R.drawable.ic_custom_category_m
    )

    Spacer(Modifier.height(12.dp))

    AppSwitch(
        lockApp = showAccountColorsInTransactions,
        onSetLockApp = onSetShowAccountColorsInTransactions,
        text = stringResource(R.string.colorful_account_labels),
        description = stringResource(R.string.colorful_account_labels_description),
        icon = R.drawable.ic_custom_palette_m
    )
}

@Composable
private fun InputAndListsSection(
    showTitleSuggestions: Boolean,
    standardKeypadLayout: Boolean,
    showCategorySearchBar: Boolean,
    sortCategoriesAscending: Boolean,
    onSetShowTitleSuggestions: (Boolean) -> Unit,
    onSetStandardKeypadLayout: (Boolean) -> Unit,
    onSetShowCategorySearchBar: (Boolean) -> Unit,
    onSetSortCategoriesAscending: (Boolean) -> Unit
) {
    AppSwitch(
        lockApp = showTitleSuggestions,
        onSetLockApp = onSetShowTitleSuggestions,
        text = stringResource(R.string.previous_title_suggestions),
        description = stringResource(R.string.previous_title_suggestions_description),
        icon = R.drawable.ic_custom_document_m
    )

    Spacer(Modifier.height(12.dp))

    AppSwitch(
        lockApp = standardKeypadLayout,
        onSetLockApp = onSetStandardKeypadLayout,
        text = stringResource(R.string.standard_keypad_layout),
        description = stringResource(R.string.standard_keypad_layout_description),
        icon = R.drawable.ic_custom_calculator_m
    )

    Spacer(Modifier.height(12.dp))

    AppSwitch(
        lockApp = showCategorySearchBar,
        onSetLockApp = onSetShowCategorySearchBar,
        text = stringResource(R.string.category_search_bar),
        description = stringResource(R.string.category_search_bar_description),
        icon = R.drawable.ic_search
    )

    Spacer(Modifier.height(12.dp))

    AppSwitch(
        lockApp = sortCategoriesAscending,
        onSetLockApp = onSetSortCategoriesAscending,
        text = stringResource(R.string.sort_categories_list),
        description = stringResource(R.string.sort_categories_list_description),
        icon = R.drawable.ic_sort_by_alpha_24
    )
}

@Composable
private fun DangerZoneSection(
    onDeleteAllDataClick: () -> Unit
) {
    SettingsSectionDivider(
        text = stringResource(R.string.danger_zone),
        color = LegacyTheme.colors.red
    )

    Spacer(Modifier.height(16.dp))

    SettingsPrimaryButton(
        icon = R.drawable.ic_delete,
        text = stringResource(R.string.delete_all_user_data),
        backgroundGradient = Gradient.solid(LegacyTheme.colors.red)
    ) {
        onDeleteAllDataClick()
    }
}

@Composable
private fun StartDateOfMonth(
    startDateOfMonth: Int,
    onClick: () -> Unit
) {
    SettingsButtonRow(
        onClick = onClick
    ) {
        Spacer(Modifier.width(12.dp))

        SettingsIcon(
            icon = R.drawable.ic_custom_calendar_m,
            tint = LegacyTheme.colors.pureInverse,
            padding = 2.dp
        )

        Spacer(Modifier.width(8.dp))

        Text(
            modifier = Modifier.padding(vertical = 20.dp),
            text = stringResource(R.string.start_date_of_month),
            style = LegacyTheme.typo.b2.style(
                color = LegacyTheme.colors.pureInverse,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(Modifier.weight(1f))

        Text(
            text = startDateOfMonth.toString(),
            style = LegacyTheme.typo.nB2.style(
                fontWeight = FontWeight.ExtraBold,
                color = LegacyTheme.colors.pureInverse
            )
        )

        Spacer(Modifier.width(32.dp))
    }
}

@Composable
private fun AppThemeButton(
    @DrawableRes icon: Int,
    label: String,
    onClick: () -> Unit
) {
    SettingsPrimaryButton(
        icon = icon,
        text = label,
        backgroundGradient = Gradient.solid(LegacyTheme.colors.medium),
        textColor = LegacyTheme.colors.pureInverse,
        iconPadding = 6.dp,
        description = stringResource(R.string.tap_to_switch_theme),
        onClick = onClick
    )
}

@Composable
private fun AppSwitch(
    lockApp: Boolean,
    onSetLockApp: (Boolean) -> Unit,
    text: String,
    icon: Int,
    description: String = "",
) {
    SettingsButtonRow(
        onClick = {
            onSetLockApp(!lockApp)
        }
    ) {
        Spacer(Modifier.width(12.dp))

        SettingsIcon(
            icon = icon,
            tint = LegacyTheme.colors.pureInverse,
            padding = 0.dp
        )

        Spacer(Modifier.width(8.dp))

        Column(
            Modifier
                .weight(1f)
                .padding(top = 20.dp, bottom = 20.dp, end = 8.dp)
        ) {
            Text(
                text = text,
                style = LegacyTheme.typo.b2.style(
                    color = LegacyTheme.colors.pureInverse,
                    fontWeight = FontWeight.Bold
                )
            )
            if (description.isNotEmpty()) {
                Text(
                    modifier = Modifier.padding(end = 8.dp),
                    text = description,
                    style = LegacyTheme.typo.nB2.style(
                        color = LegacyTheme.colors.gray,
                        fontWeight = FontWeight.Normal
                    ).copy(fontSize = 14.sp)
                )
            }
        }

        // Spacer(Modifier.weight(1f))

        SettingsSwitch(enabled = lockApp) {
            onSetLockApp(it)
        }

        Spacer(Modifier.width(16.dp))
    }
}

@Composable
private fun ExportCSV(
    onExportToCSV: () -> Unit
) {
    SettingsDefaultButton(
        icon = R.drawable.ic_vue_pc_printer,
        text = stringResource(R.string.export_to_csv),
        iconPadding = 6.dp,
        description = stringResource(R.string.do_not_use_for_backup_purposes)
    ) {
        onExportToCSV()
    }
}

@Composable
private fun SettingsPrimaryButton(
    @DrawableRes icon: Int,
    text: String,
    hasShadow: Boolean = false,
    backgroundGradient: Gradient = Gradient.solid(LegacyTheme.colors.medium),
    textColor: Color = White,
    iconPadding: Dp = 0.dp,
    description: String? = null,
    onClick: () -> Unit
) {
    SettingsButtonRow(
        hasShadow = hasShadow,
        backgroundGradient = backgroundGradient,
        onClick = onClick
    ) {
        Spacer(Modifier.width(12.dp))

        SettingsIcon(
            icon = icon,
            tint = textColor,
            padding = iconPadding
        )

        Spacer(Modifier.width(8.dp))

        Column(
            Modifier
                .weight(1f)
                .padding(top = 20.dp, bottom = 20.dp, end = 8.dp)
        ) {
            Text(
                text = text,
                style = LegacyTheme.typo.b2.style(
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                )
            )
            if (!description.isNullOrEmpty()) {
                Text(
                    modifier = Modifier.padding(end = 8.dp),
                    text = description,
                    style = LegacyTheme.typo.nB2.style(
                        color = LegacyTheme.colors.gray,
                        fontWeight = FontWeight.Normal
                    ).copy(fontSize = 14.sp)
                )
            }
        }
    }
}

@Composable
private fun SettingsButtonRow(
    onClick: (() -> Unit)?,
    hasShadow: Boolean = false,
    backgroundGradient: Gradient = Gradient.solid(LegacyTheme.colors.medium),
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .thenIf(hasShadow) {
                drawColoredShadow(color = backgroundGradient.startColor)
            }
            .fillMaxWidth()
            .clip(LegacyTheme.shapes.r4)
            .background(backgroundGradient.asHorizontalBrush(), LegacyTheme.shapes.r4)
            .thenIf(onClick != null) {
                clickable {
                    onClick?.invoke()
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}

@Composable
private fun CurrencyButton(
    currency: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(LegacyTheme.shapes.r4)
            .border(2.dp, LegacyTheme.colors.medium, LegacyTheme.shapes.r4)
            .clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(12.dp))

        SettingsIcon(
            icon = R.drawable.ic_currency,
            padding = 0.dp
        )

        Spacer(Modifier.width(8.dp))

        Text(
            modifier = Modifier.padding(vertical = 20.dp),
            text = stringResource(R.string.set_currency),
            style = LegacyTheme.typo.b2.style(
                color = LegacyTheme.colors.pureInverse,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(Modifier.weight(1f))

        Text(
            text = currency,
            style = LegacyTheme.typo.b1.style(
                color = LegacyTheme.colors.pureInverse,
                fontWeight = FontWeight.ExtraBold
            )
        )

        Spacer(Modifier.height(4.dp))

        SettingsIcon(
            icon = R.drawable.ic_arrow_right,
        )

        Spacer(Modifier.width(24.dp))
    }
}

@Composable
private fun SettingsSectionDivider(
    text: String,
    color: Color? = null
) {
    val dividerColor = color ?: LegacyTheme.colors.gray

    Column {
        Spacer(Modifier.height(32.dp))

        Text(
            modifier = Modifier.padding(start = 32.dp),
            text = text,
            style = LegacyTheme.typo.b2.style(
                color = dividerColor,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun SettingsSubMenuButton(
    @DrawableRes icon: Int,
    text: String,
    onClick: () -> Unit,
) {
    SettingsButtonRow(
        backgroundGradient = Gradient.solid(LegacyTheme.colors.medium),
        onClick = onClick
    ) {
        Spacer(Modifier.width(12.dp))

        SettingsIcon(
            icon = icon,
            tint = LegacyTheme.colors.pureInverse,
        )

        Spacer(Modifier.width(8.dp))

        Text(
            modifier = Modifier
                .weight(1f)
                .padding(top = 20.dp, bottom = 20.dp, end = 8.dp),
            text = text,
            style = LegacyTheme.typo.b2.style(
                color = LegacyTheme.colors.pureInverse,
                fontWeight = FontWeight.Bold
            )
        )

        SettingsIcon(
            icon = R.drawable.ic_arrow_right,
        )

        Spacer(Modifier.width(24.dp))
    }
}

@Composable
private fun SettingsDefaultButton(
    @DrawableRes icon: Int,
    text: String,
    iconPadding: Dp = 0.dp,
    description: String? = null,
    onClick: () -> Unit,
) {
    SettingsPrimaryButton(
        icon = icon,
        text = text,
        backgroundGradient = Gradient.solid(LegacyTheme.colors.medium),
        textColor = LegacyTheme.colors.pureInverse,
        iconPadding = iconPadding,
        description = description
    ) {
        onClick()
    }
}

@Composable
private fun SettingsIcon(
    @DrawableRes icon: Int,
    tint: Color = LegacyTheme.colors.pureInverse,
    padding: Dp = 4.dp,
) {
    Image(
        modifier = Modifier
            .size(48.dp)
            .padding(all = padding),
        painter = painterResource(id = icon),
        colorFilter = ColorFilter.tint(tint),
        alignment = Alignment.Center,
        contentScale = ContentScale.Fit,
        contentDescription = "icon"
    )
}
