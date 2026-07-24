package com.ivy.settings

import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.ivy.ui.compose.BackPressHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.data.model.GitHubBackupConfig
import com.ivy.data.model.Theme
import com.ivy.ui.compose.thenIf
import com.ivy.ui.compose.drawColoredShadow
import com.ivy.ui.navigation.ImportScreen
import com.ivy.ui.navigation.MainScreen
import com.ivy.ui.navigation.navigation
import com.ivy.ui.navigation.screenScopedViewModel
import com.ivy.ui.platform.buildInfoProvider
import com.ivy.ui.platform.fileSharer
import com.ivy.ui.R
import com.ivy.ui.theme.colors.Gradient
import com.ivy.ui.theme.colors.IvyGradients
import com.ivy.ui.theme.colors.IvyFixedColors
import com.ivy.ui.theme.colors.IvyFixedColors.White
import com.ivy.ui.modal.DeleteModal
import com.ivy.ui.modal.ProgressModal
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private enum class SettingsPage(@StringRes val title: Int) {
    Main(R.string.settings),
    DisplayPreferences(R.string.display_preferences),
    InputAndLists(R.string.input_and_lists)
}

@ExperimentalFoundationApi
@Composable
fun BoxWithConstraintsScope.SettingsScreen() {
    SettingsScreenContent(embedded = false)
}

@ExperimentalFoundationApi
@Composable
fun BoxWithConstraintsScope.SettingsTab() {
    SettingsScreenContent(embedded = true)
}

@ExperimentalFoundationApi
@Composable
private fun BoxWithConstraintsScope.SettingsScreenContent(embedded: Boolean) {
    val viewModel: SettingsViewModel = screenScopedViewModel()
    val uiState = viewModel.uiState()
    val fileSharer = fileSharer()
    val nav = navigation()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                SettingsUiEvent.WalletDataReset -> {
                    nav.resetBackStack()
                    nav.navigateTo(MainScreen)
                }

                is SettingsUiEvent.ShareCsvFile -> fileSharer.shareCSVFile(event.fileUri)
                is SettingsUiEvent.ShareZipFile -> fileSharer.shareZipFile(event.fileUri)
                is SettingsUiEvent.ShowMessage ->
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()

                SettingsUiEvent.DataRestored -> {
                    nav.resetBackStack()
                    nav.navigateTo(MainScreen)
                }
            }
        }
    }

    UI(
        accountsCount = uiState.accountsCount,
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
        onBackupData = {
            viewModel.onEvent(SettingsEvent.BackupData)
        },
        gitHubConfig = uiState.gitHubBackupConfig,
        gitHubLastBackupEpochSec = uiState.gitHubLastBackupEpochSec,
        onSaveGitHubConfig = {
            viewModel.onEvent(SettingsEvent.SaveGitHubBackupConfig(it))
        },
        onClearGitHubConfig = {
            viewModel.onEvent(SettingsEvent.ClearGitHubBackupConfig)
        },
        onTestGitHubConnection = {
            viewModel.onEvent(SettingsEvent.TestGitHubConnection(it))
        },
        onBackupToGitHub = {
            viewModel.onEvent(SettingsEvent.BackupToGitHub)
        },
        onRestoreFromGitHub = {
            viewModel.onEvent(SettingsEvent.RestoreFromGitHub)
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
        onBack = nav::back,
        onOpenImport = {
            nav.navigateTo(ImportScreen)
        },
        embedded = embedded,
    )
}

@ExperimentalFoundationApi
@Composable
@Suppress("LongMethod")
private fun BoxWithConstraintsScope.UI(
    accountsCount: Int = 0,
    theme: Theme,
    onSwitchTheme: () -> Unit,
    lockApp: Boolean,
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
    gitHubConfig: GitHubBackupConfig? = null,
    gitHubLastBackupEpochSec: Long? = null,
    onBackupData: () -> Unit = {},
    onSaveGitHubConfig: (GitHubBackupConfig) -> Unit = {},
    onClearGitHubConfig: () -> Unit = {},
    onTestGitHubConnection: (GitHubBackupConfig) -> Unit = {},
    onBackupToGitHub: () -> Unit = {},
    onRestoreFromGitHub: () -> Unit = {},
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
    onBack: () -> Unit = {},
    onOpenImport: () -> Unit = {},
    embedded: Boolean = false,
) {
    var chooseStartDateOfMonthVisible by remember { mutableStateOf(false) }
    var deleteAllDataModalVisible by remember { mutableStateOf(false) }
    var deleteAllDataModalFinalVisible by remember { mutableStateOf(false) }
    var gitHubBackupModalVisible by remember { mutableStateOf(false) }
    var restoreFromGitHubConfirmVisible by remember { mutableStateOf(false) }
    var settingsPage by remember { mutableStateOf(SettingsPage.Main) }
    val mainListState = rememberLazyListState()
    val displayPreferencesListState = rememberLazyListState()
    val inputAndListsListState = rememberLazyListState()
    val currentListState = when (settingsPage) {
        SettingsPage.Main -> mainListState
        SettingsPage.DisplayPreferences -> displayPreferencesListState
        SettingsPage.InputAndLists -> inputAndListsListState
    }
    BackPressHandler(enabled = settingsPage != SettingsPage.Main) {
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
        if (!embedded || settingsPage != SettingsPage.Main) {
            stickyHeader {
                SettingsToolbarFrame(
                    onBack = {
                        if (settingsPage == SettingsPage.Main) {
                            onBack()
                        } else {
                            settingsPage = SettingsPage.Main
                        }
                    },
                )
            }
        }

        item {
            Spacer(Modifier.height(8.dp))

            Text(
                modifier = Modifier.padding(start = 32.dp),
                text = stringResource(
                    if (settingsPage == SettingsPage.Main) R.string.settings else settingsPage.title
                ),
                style = SettingsTheme.typo.h2.copy(
                    color = SettingsTheme.colors.pureInverse,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Start
                )
            )

            Spacer(Modifier.height(16.dp))
        }

        when (settingsPage) {
            SettingsPage.Main -> {
                item {
                    ProfileHeroCard(
                        accountsCount = accountsCount,
                    )
                }

                item {
                    Spacer(Modifier.height(16.dp))

                    CloudBackupHeroCard(
                        configured = gitHubConfig != null,
                        subtitle = gitHubBackupSubtitle(gitHubConfig, gitHubLastBackupEpochSec),
                        onConfigure = { gitHubBackupModalVisible = true },
                        onBackup = onBackupToGitHub,
                        onRestore = { restoreFromGitHubConfirmVisible = true }
                    )
                }

                item {
                    GeneralSection(
                        startDateOfMonth = startDateOfMonth,
                        lockApp = lockApp,
                        showNotifications = showNotifications,
                        onStartDateClick = { chooseStartDateOfMonthVisible = true },
                        onSetLockApp = onSetLockApp,
                        onSetShowNotifications = onSetShowNotifications,
                        onOpenDisplayPreferences = { settingsPage = SettingsPage.DisplayPreferences },
                        onOpenInputAndLists = { settingsPage = SettingsPage.InputAndLists },
                    )
                }

                item {
                    FeaturesSection(
                        treatTransfersAsIncomeExpense = treatTransfersAsIncomeExpense,
                        onSetTreatTransfersAsIncExp = onSetTreatTransfersAsIncExp,
                    )
                }

                item {
                    DataManagementSection(
                        onExportToCSV = onExportToCSV,
                        onBackupData = onBackupData,
                        onImportData = onOpenImport
                    )
                }

                item {
                    AboutSection()
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

    GitHubBackupModal(
        visible = gitHubBackupModalVisible,
        initialConfig = gitHubConfig,
        onSave = onSaveGitHubConfig,
        onClear = onClearGitHubConfig,
        onTest = onTestGitHubConnection,
        dismiss = { gitHubBackupModalVisible = false }
    )

    DeleteModal(
        title = "从 GitHub 恢复？",
        description = "将用云端最新备份覆盖并合并到本地数据，建议先备份当前数据。",
        visible = restoreFromGitHubConfirmVisible,
        buttonText = "恢复",
        iconStart = R.drawable.ic_export_csv,
        dismiss = { restoreFromGitHubConfirmVisible = false },
        onDelete = {
            restoreFromGitHubConfirmVisible = false
            onRestoreFromGitHub()
        }
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
            .background(SettingsTheme.colors.pure)
            .padding(top = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(20.dp))

        Icon(
            modifier = Modifier
                .testTag("toolbar_back")
                .clip(CircleShape)
                .background(SettingsTheme.colors.pure, CircleShape)
                .border(2.dp, SettingsTheme.colors.medium, CircleShape)
                .clickable(onClick = onBack)
                .padding(6.dp),
            painter = painterResource(id = R.drawable.ic_back),
            contentDescription = "back",
            tint = SettingsTheme.colors.pureInverse,
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

private fun gitHubBackupSubtitle(
    config: GitHubBackupConfig?,
    lastBackupEpochSec: Long?
): String {
    if (config == null) {
        return "未配置，点击设置 Token 与仓库"
    }
    val repo = "${config.owner}/${config.repo}"
    val last = lastBackupEpochSec?.let {
        val formatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault())
        "上次备份 ${formatter.format(Instant.ofEpochSecond(it))}"
    } ?: "尚未备份"
    return "$repo · $last"
}

@Composable
private fun CloudBackupHeroCard(
    configured: Boolean,
    subtitle: String,
    onConfigure: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .drawColoredShadow(color = IvyGradients.Ivy.startColor)
            .clip(SettingsTheme.shapes.r4)
            .background(IvyGradients.Ivy.asHorizontalBrush(), SettingsTheme.shapes.r4)
            .clickable(onClick = onConfigure)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SettingsIcon(
                icon = R.drawable.ic_vue_security_shield,
                tint = White,
                padding = 8.dp,
            )

            Spacer(Modifier.width(8.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = "GitHub 云备份",
                    style = SettingsTheme.typo.b1.copy(
                        color = White,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Start
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = SettingsTheme.typo.nB2.copy(
                        color = White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Start
                    ).copy(fontSize = 13.sp)
                )
            }

            Spacer(Modifier.width(8.dp))

            BackupStatusPill(configured = configured)
        }

        Spacer(Modifier.height(14.dp))

        if (configured) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                HeroActionButton(
                    text = "立即备份",
                    filled = true,
                    modifier = Modifier.weight(1f),
                    onClick = onBackup,
                )
                Spacer(Modifier.width(12.dp))
                HeroActionButton(
                    text = "恢复",
                    filled = false,
                    modifier = Modifier.weight(1f),
                    onClick = onRestore,
                )
            }
        } else {
            HeroActionButton(
                text = "配置 GitHub 备份",
                filled = true,
                modifier = Modifier.fillMaxWidth(),
                onClick = onConfigure,
            )
        }
    }
}

@Composable
private fun BackupStatusPill(configured: Boolean) {
    Text(
        modifier = Modifier
            .clip(CircleShape)
            .background(White.copy(alpha = 0.18f))
            .padding(horizontal = 12.dp, vertical = 5.dp),
        text = if (configured) "已配置" else "未配置",
        style = SettingsTheme.typo.nC.copy(
            color = White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start
        )
    )
}

@Composable
private fun HeroActionButton(
    text: String,
    filled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .then(
                if (filled) {
                    Modifier.background(White)
                } else {
                    Modifier.border(1.5.dp, White.copy(alpha = 0.55f), CircleShape)
                }
            )
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = SettingsTheme.typo.b2.copy(
                color = if (filled) IvyFixedColors.Ivy else White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )
    }
}

@Composable
private fun ProfileHeroCard(
    accountsCount: Int,
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(SettingsTheme.shapes.r4)
            .background(SettingsTheme.colors.medium, SettingsTheme.shapes.r4)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(IvyGradients.Ivy.asHorizontalBrush()),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = R.drawable.ic_custom_account_m),
                contentDescription = "wallet",
                tint = White,
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.my_wallet),
                style = SettingsTheme.typo.b1.copy(
                    color = SettingsTheme.colors.pureInverse,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Start
                )
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.local_ledger_accounts, accountsCount),
                style = SettingsTheme.typo.nB2.copy(
                    color = SettingsTheme.colors.gray,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Start
                ).copy(fontSize = 13.sp)
            )
        }
    }
}

@Composable
private fun GeneralSection(
    startDateOfMonth: Int,
    lockApp: Boolean,
    showNotifications: Boolean,
    onStartDateClick: () -> Unit,
    onSetLockApp: (Boolean) -> Unit,
    onSetShowNotifications: (Boolean) -> Unit,
    onOpenDisplayPreferences: () -> Unit,
    onOpenInputAndLists: () -> Unit,
) {
    SettingsSectionDivider(text = stringResource(R.string.general))

    Spacer(Modifier.height(16.dp))

    StartDateOfMonth(startDateOfMonth = startDateOfMonth) {
        onStartDateClick()
    }

    Spacer(Modifier.height(12.dp))

    SettingsSubMenuButton(
        icon = R.drawable.ic_custom_palette_m,
        text = stringResource(R.string.display_preferences)
    ) {
        onOpenDisplayPreferences()
    }

    Spacer(Modifier.height(12.dp))

    SettingsSubMenuButton(
        icon = R.drawable.ic_custom_document_m,
        text = stringResource(R.string.input_and_lists)
    ) {
        onOpenInputAndLists()
    }

    Spacer(Modifier.height(12.dp))

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
}

@Composable
private fun FeaturesSection(
    treatTransfersAsIncomeExpense: Boolean,
    onSetTreatTransfersAsIncExp: (Boolean) -> Unit,
) {
    SettingsSectionDivider(text = stringResource(R.string.features))

    Spacer(Modifier.height(16.dp))

    AppSwitch(
        lockApp = treatTransfersAsIncomeExpense,
        onSetLockApp = onSetTreatTransfersAsIncExp,
        text = stringResource(R.string.transfers_as_income_expense),
        description = stringResource(R.string.transfers_as_income_expense_description),
        icon = R.drawable.ic_custom_transfer_m
    )
}

@Composable
private fun AboutSection() {
    SettingsSectionDivider(text = stringResource(R.string.about))

    Spacer(Modifier.height(16.dp))

    val buildInfoProvider = buildInfoProvider()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(SettingsTheme.shapes.r4)
            .border(2.dp, SettingsTheme.colors.medium, SettingsTheme.shapes.r4),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(12.dp))

        SettingsIcon(
            icon = R.drawable.ic_custom_document_m,
            padding = 0.dp
        )

        Spacer(Modifier.width(8.dp))

        Text(
            modifier = Modifier.padding(vertical = 14.dp),
            text = stringResource(R.string.version),
            style = SettingsTheme.typo.b2.copy(
                color = SettingsTheme.colors.pureInverse,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start
            )
        )

        Spacer(Modifier.weight(1f))

        Text(
            text = "${buildInfoProvider.buildVersionName} (${buildInfoProvider.buildVersionCode})",
            style = SettingsTheme.typo.nB2.copy(
                color = SettingsTheme.colors.gray,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End
            )
        )

        Spacer(Modifier.width(24.dp))
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
        color = SettingsTheme.colors.red
    )

    Spacer(Modifier.height(16.dp))

    SettingsPrimaryButton(
        icon = R.drawable.ic_delete,
        text = stringResource(R.string.delete_all_user_data),
        backgroundGradient = Gradient.solid(SettingsTheme.colors.red)
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
            tint = SettingsTheme.colors.pureInverse,
            padding = 2.dp
        )

        Spacer(Modifier.width(8.dp))

        Text(
            modifier = Modifier.padding(vertical = 14.dp),
            text = stringResource(R.string.start_date_of_month),
            style = SettingsTheme.typo.b2.copy(
                color = SettingsTheme.colors.pureInverse,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start
            )
        )

        Spacer(Modifier.weight(1f))

        Text(
            text = startDateOfMonth.toString(),
            style = SettingsTheme.typo.nB2.copy(
                fontWeight = FontWeight.ExtraBold,
                color = SettingsTheme.colors.pureInverse,
                textAlign = TextAlign.Start
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
        backgroundGradient = Gradient.solid(SettingsTheme.colors.medium),
        textColor = SettingsTheme.colors.pureInverse,
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
            tint = SettingsTheme.colors.pureInverse,
            padding = 0.dp
        )

        Spacer(Modifier.width(8.dp))

        Column(
            Modifier
                .weight(1f)
                .padding(top = 14.dp, bottom = 14.dp, end = 8.dp)
        ) {
            Text(
                text = text,
                style = SettingsTheme.typo.b2.copy(
                    color = SettingsTheme.colors.pureInverse,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start
                )
            )
            if (description.isNotEmpty()) {
                Text(
                    modifier = Modifier.padding(end = 8.dp),
                    text = description,
                    style = SettingsTheme.typo.nB2.copy(
                        color = SettingsTheme.colors.gray,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Start
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
    backgroundGradient: Gradient = Gradient.solid(SettingsTheme.colors.medium),
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
                .padding(top = 14.dp, bottom = 14.dp, end = 8.dp)
        ) {
            Text(
                text = text,
                style = SettingsTheme.typo.b2.copy(
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start
                )
            )
            if (!description.isNullOrEmpty()) {
                Text(
                    modifier = Modifier.padding(end = 8.dp),
                    text = description,
                    style = SettingsTheme.typo.nB2.copy(
                        color = SettingsTheme.colors.gray,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Start
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
    backgroundGradient: Gradient = Gradient.solid(SettingsTheme.colors.medium),
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .thenIf(hasShadow) {
                drawColoredShadow(color = backgroundGradient.startColor)
            }
            .fillMaxWidth()
            .clip(SettingsTheme.shapes.r4)
            .background(backgroundGradient.asHorizontalBrush(), SettingsTheme.shapes.r4)
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
private fun SettingsSectionDivider(
    text: String,
    color: Color? = null
) {
    val dividerColor = color ?: SettingsTheme.colors.gray

    Column {
        Spacer(Modifier.height(20.dp))

        Text(
            modifier = Modifier.padding(start = 32.dp),
            text = text,
            style = SettingsTheme.typo.b2.copy(
                color = dividerColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start
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
        backgroundGradient = Gradient.solid(SettingsTheme.colors.medium),
        onClick = onClick
    ) {
        Spacer(Modifier.width(12.dp))

        SettingsIcon(
            icon = icon,
            tint = SettingsTheme.colors.pureInverse,
        )

        Spacer(Modifier.width(8.dp))

        Text(
            modifier = Modifier
                .weight(1f)
                .padding(top = 14.dp, bottom = 14.dp, end = 8.dp),
            text = text,
            style = SettingsTheme.typo.b2.copy(
                color = SettingsTheme.colors.pureInverse,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start
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
        backgroundGradient = Gradient.solid(SettingsTheme.colors.medium),
        textColor = SettingsTheme.colors.pureInverse,
        iconPadding = iconPadding,
        description = description
    ) {
        onClick()
    }
}

@Composable
private fun SettingsIcon(
    @DrawableRes icon: Int,
    tint: Color = SettingsTheme.colors.pureInverse,
    padding: Dp = 4.dp,
) {
    Image(
        modifier = Modifier
            .size(40.dp)
            .padding(all = padding),
        painter = painterResource(id = icon),
        colorFilter = ColorFilter.tint(tint),
        alignment = Alignment.Center,
        contentScale = ContentScale.Fit,
        contentDescription = "icon"
    )
}
