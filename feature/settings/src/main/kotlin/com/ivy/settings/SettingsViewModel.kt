package com.ivy.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.ivy.data.model.Theme
import com.ivy.data.model.ExternalFile
import com.ivy.data.model.GitHubBackupConfig
import com.ivy.data.model.primitive.AssetCode
import com.ivy.domain.preferences.toggles.BoolPreference
import com.ivy.domain.preferences.toggles.PreferenceToggleService
import com.ivy.domain.preferences.toggles.PreferenceToggleCatalog
import com.ivy.domain.usecase.reset.ResetWalletDataUseCase
import com.ivy.domain.usecase.backup.ExportBackupUseCase
import com.ivy.domain.usecase.backup.github.GetGitHubBackupConfigUseCase
import com.ivy.domain.usecase.backup.github.GetGitHubLastBackupUseCase
import com.ivy.domain.usecase.backup.github.RestoreBackupFromGitHubUseCase
import com.ivy.domain.usecase.backup.github.SaveGitHubBackupConfigUseCase
import com.ivy.domain.usecase.backup.github.TestGitHubConnectionUseCase
import com.ivy.domain.usecase.backup.github.UploadBackupToGitHubUseCase
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.domain.usecase.currency.SetBaseCurrencyUseCase
import com.ivy.domain.usecase.csv.ExportCsvUseCase
import com.ivy.domain.usecase.exchange.SyncExchangeRatesUseCase
import com.ivy.domain.usecase.settings.GetAppLockEnabledPreferenceUseCase
import com.ivy.domain.usecase.settings.GetHideCurrentBalancePreferenceUseCase
import com.ivy.domain.usecase.settings.GetHideIncomePreferenceUseCase
import com.ivy.domain.usecase.settings.GetShowNotificationsPreferenceUseCase
import com.ivy.domain.usecase.settings.GetStartDayOfMonthUseCase
import com.ivy.domain.usecase.settings.GetThemeUseCase
import com.ivy.domain.usecase.settings.GetTransfersAsIncomeExpensePreferenceUseCase
import com.ivy.domain.usecase.settings.SetAppLockEnabledPreferenceUseCase
import com.ivy.domain.usecase.settings.SetHideCurrentBalancePreferenceUseCase
import com.ivy.domain.usecase.settings.SetHideIncomePreferenceUseCase
import com.ivy.domain.usecase.settings.SetShowNotificationsPreferenceUseCase
import com.ivy.domain.usecase.settings.SetStartDayOfMonthUseCase
import com.ivy.domain.usecase.settings.SetTransfersAsIncomeExpensePreferenceUseCase
import com.ivy.domain.usecase.settings.SwitchThemeUseCase
import com.ivy.ui.theme.ThemeState
import com.ivy.ui.period.PeriodState
import com.ivy.ui.ComposeViewModel
import com.ivy.ui.platform.FilePicker
import com.ivy.ui.platform.LocaleSettingsLauncher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject

private val exportTimestampFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")

@Stable
@HiltViewModel
internal class SettingsViewModel @Inject internal constructor(
    private val themeState: ThemeState,
    private val periodState: PeriodState,
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val setBaseCurrency: SetBaseCurrencyUseCase,
    private val getTheme: GetThemeUseCase,
    private val switchThemeUseCase: SwitchThemeUseCase,
    private val resetWalletDataUseCase: ResetWalletDataUseCase,
    private val exportBackupUseCase: ExportBackupUseCase,
    private val getGitHubBackupConfigUseCase: GetGitHubBackupConfigUseCase,
    private val saveGitHubBackupConfigUseCase: SaveGitHubBackupConfigUseCase,
    private val getGitHubLastBackupUseCase: GetGitHubLastBackupUseCase,
    private val testGitHubConnectionUseCase: TestGitHubConnectionUseCase,
    private val uploadBackupToGitHubUseCase: UploadBackupToGitHubUseCase,
    private val restoreBackupFromGitHubUseCase: RestoreBackupFromGitHubUseCase,
    private val getStartDayOfMonth: GetStartDayOfMonthUseCase,
    private val setStartDayOfMonth: SetStartDayOfMonthUseCase,
    private val getAppLockEnabledPreference: GetAppLockEnabledPreferenceUseCase,
    private val setAppLockEnabledPreference: SetAppLockEnabledPreferenceUseCase,
    private val getHideCurrentBalancePreference: GetHideCurrentBalancePreferenceUseCase,
    private val setHideCurrentBalancePreference: SetHideCurrentBalancePreferenceUseCase,
    private val getHideIncomePreference: GetHideIncomePreferenceUseCase,
    private val setHideIncomePreference: SetHideIncomePreferenceUseCase,
    private val getShowNotificationsPreference: GetShowNotificationsPreferenceUseCase,
    private val setShowNotificationsPreference: SetShowNotificationsPreferenceUseCase,
    private val getTransfersAsIncomeExpensePreference: GetTransfersAsIncomeExpensePreferenceUseCase,
    private val setTransfersAsIncomeExpensePreference: SetTransfersAsIncomeExpensePreferenceUseCase,
    private val syncExchangeRatesUseCase: SyncExchangeRatesUseCase,
    private val preferenceToggles: PreferenceToggleCatalog,
    private val preferenceToggleService: PreferenceToggleService,
    private val filePicker: FilePicker,
    private val exportCsvUseCase: ExportCsvUseCase,
    private val localeSettingsLauncher: LocaleSettingsLauncher
) : ComposeViewModel<SettingsState, SettingsEvent>() {

    private val currencyCode = mutableStateOf("")
    private val currentTheme = mutableStateOf<Theme>(Theme.AUTO)
    private val lockApp = mutableStateOf(false)
    private val showNotifications = mutableStateOf(true)
    private val hideCurrentBalance = mutableStateOf(false)
    private val hideIncome = mutableStateOf(false)
    private val treatTransfersAsIncomeExpense = mutableStateOf(false)
    private val compactAccountsMode = mutableStateOf(false)
    private val hideAccountTotalBalance = mutableStateOf(false)
    private val compactCategoriesMode = mutableStateOf(false)
    private val showAccountColorsInTransactions = mutableStateOf(false)
    private val showTitleSuggestions = mutableStateOf(true)
    private val standardKeypadLayout = mutableStateOf(false)
    private val showCategorySearchBar = mutableStateOf(true)
    private val sortCategoriesAscending = mutableStateOf(false)
    private val showPlannedPaymentsQuickAccess = mutableStateOf(false)
    private val showBudgetsQuickAccess = mutableStateOf(false)
    private val showLoansQuickAccess = mutableStateOf(false)
    private val startDateOfMonth = mutableIntStateOf(1)
    private val progressState = mutableStateOf(false)
    private val gitHubBackupConfig = mutableStateOf<GitHubBackupConfig?>(null)
    private val gitHubLastBackupEpochSec = mutableStateOf<Long?>(null)
    private val _uiEvents = MutableSharedFlow<SettingsUiEvent>()
    val uiEvents: SharedFlow<SettingsUiEvent> = _uiEvents.asSharedFlow()

    @Composable
    override fun uiState(): SettingsState {
        LaunchedEffect(Unit) {
            onStart()
        }

        return SettingsState(
            currencyCode = getCurrencyCode(),
            currentTheme = getCurrentTheme(),
            gitHubBackupConfig = gitHubBackupConfig.value,
            gitHubLastBackupEpochSec = gitHubLastBackupEpochSec.value,
            lockApp = getLockApp(),
            showNotifications = getShowNotifications(),
            hideCurrentBalance = getHideCurrentBalance(),
            treatTransfersAsIncomeExpense = getTreatTransfersAsIncomeExpense(),
            compactAccountsMode = getCompactAccountsMode(),
            hideAccountTotalBalance = getHideAccountTotalBalance(),
            compactCategoriesMode = getCompactCategoriesMode(),
            showAccountColorsInTransactions = getShowAccountColorsInTransactions(),
            showTitleSuggestions = getShowTitleSuggestions(),
            standardKeypadLayout = getStandardKeypadLayout(),
            showCategorySearchBar = getShowCategorySearchBar(),
            sortCategoriesAscending = getSortCategoriesAscending(),
            showPlannedPaymentsQuickAccess = showPlannedPaymentsQuickAccess.value,
            showBudgetsQuickAccess = showBudgetsQuickAccess.value,
            showLoansQuickAccess = showLoansQuickAccess.value,
            startDateOfMonth = getStartDateOfMonth(),
            progressState = getProgressState(),
            hideIncome = getHideIncome(),
            languageOptionVisible = isLanguageOptionVisible()
        )
    }

    private suspend fun onStart() {
        initializeCurrency()
        initializeCurrentTheme()
        initializeLockApp()
        initializeShowNotifications()
        initializeHideCurrentBalance()
        initializeHideIncome()
        initializeTransfersAsIncomeExpense()
        initializeTogglePreferences()
        initializeStartDateOfMonth()
        initializeGitHubBackup()
    }

    private fun initializeGitHubBackup() {
        gitHubBackupConfig.value = getGitHubBackupConfigUseCase()
        gitHubLastBackupEpochSec.value = getGitHubLastBackupUseCase()
    }

    private suspend fun initializeCurrency() {
        currencyCode.value = getBaseCurrencyCode()
    }

    private suspend fun initializeCurrentTheme() {
        currentTheme.value = getTheme()
    }

    private fun initializeLockApp() {
        lockApp.value = getAppLockEnabledPreference()
    }

    private fun initializeShowNotifications() {
        showNotifications.value = getShowNotificationsPreference()
    }

    private fun initializeHideCurrentBalance() {
        hideCurrentBalance.value = getHideCurrentBalancePreference()
    }

    private fun initializeHideIncome() {
        hideIncome.value = getHideIncomePreference()
    }

    private fun initializeTransfersAsIncomeExpense() {
        treatTransfersAsIncomeExpense.value = getTransfersAsIncomeExpensePreference()
    }

    private suspend fun initializeTogglePreferences() {
        compactAccountsMode.value = preferenceToggleService.isEnabled(preferenceToggles.compactAccountsMode)
        hideAccountTotalBalance.value = preferenceToggleService.isEnabled(preferenceToggles.hideTotalBalance)
        compactCategoriesMode.value = preferenceToggleService.isEnabled(preferenceToggles.compactCategoriesMode)
        showAccountColorsInTransactions.value =
            preferenceToggleService.isEnabled(preferenceToggles.showAccountColorsInTransactions)
        showTitleSuggestions.value = preferenceToggleService.isEnabled(preferenceToggles.showTitleSuggestions)
        standardKeypadLayout.value = preferenceToggleService.isEnabled(preferenceToggles.standardKeypadLayout)
        showCategorySearchBar.value = preferenceToggleService.isEnabled(preferenceToggles.showCategorySearchBar)
        sortCategoriesAscending.value = preferenceToggleService.isEnabled(preferenceToggles.sortCategoriesAscending)
        showPlannedPaymentsQuickAccess.value =
            preferenceToggleService.isEnabled(preferenceToggles.showPlannedPaymentsQuickAccess)
        showBudgetsQuickAccess.value =
            preferenceToggleService.isEnabled(preferenceToggles.showBudgetsQuickAccess)
        showLoansQuickAccess.value =
            preferenceToggleService.isEnabled(preferenceToggles.showLoansQuickAccess)
    }

    private fun initializeStartDateOfMonth() {
        val startDay = getStartDayOfMonth()
        periodState.updateStartDayOfMonth(startDay)
        startDateOfMonth.intValue = startDay
    }

    @Composable
    private fun getCurrencyCode(): String {
        return currencyCode.value
    }

    @Composable
    private fun getCurrentTheme(): Theme {
        return currentTheme.value
    }

    @Composable
    private fun getLockApp(): Boolean {
        return lockApp.value
    }

    @Composable
    private fun getShowNotifications(): Boolean {
        return showNotifications.value
    }

    @Composable
    private fun getHideCurrentBalance(): Boolean {
        return hideCurrentBalance.value
    }

    @Composable
    private fun getHideIncome(): Boolean {
        return hideIncome.value
    }

    @Composable
    private fun getTreatTransfersAsIncomeExpense(): Boolean {
        return treatTransfersAsIncomeExpense.value
    }

    @Composable
    private fun getCompactAccountsMode(): Boolean {
        return compactAccountsMode.value
    }

    @Composable
    private fun getHideAccountTotalBalance(): Boolean {
        return hideAccountTotalBalance.value
    }

    @Composable
    private fun getCompactCategoriesMode(): Boolean {
        return compactCategoriesMode.value
    }

    @Composable
    private fun getShowAccountColorsInTransactions(): Boolean {
        return showAccountColorsInTransactions.value
    }

    @Composable
    private fun getShowTitleSuggestions(): Boolean {
        return showTitleSuggestions.value
    }

    @Composable
    private fun getStandardKeypadLayout(): Boolean {
        return standardKeypadLayout.value
    }

    @Composable
    private fun getShowCategorySearchBar(): Boolean {
        return showCategorySearchBar.value
    }

    @Composable
    private fun getSortCategoriesAscending(): Boolean {
        return sortCategoriesAscending.value
    }

    @Composable
    private fun getStartDateOfMonth(): String {
        return startDateOfMonth.intValue.toString()
    }

    @Composable
    private fun getProgressState(): Boolean {
        return progressState.value
    }

    private fun isLanguageOptionVisible(): Boolean {
        return localeSettingsLauncher.appLocaleSettingsAvailable
    }

    override fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.SetCurrency -> setCurrency(event.newCurrency)
            SettingsEvent.ExportToCsv -> exportToCSV()
            SettingsEvent.BackupData -> exportToZip()
            is SettingsEvent.SaveGitHubBackupConfig -> saveGitHubBackupConfig(event.config)
            SettingsEvent.ClearGitHubBackupConfig -> clearGitHubBackupConfig()
            is SettingsEvent.TestGitHubConnection -> testGitHubConnection(event.config)
            SettingsEvent.BackupToGitHub -> backupToGitHub()
            SettingsEvent.RestoreFromGitHub -> restoreFromGitHub()
            SettingsEvent.SwitchTheme -> switchTheme()
            is SettingsEvent.SetLockApp -> setLockApp(event.lockApp)
            is SettingsEvent.SetShowNotifications -> setShowNotifications(event.showNotifications)
            is SettingsEvent.SetHideCurrentBalance -> setHideCurrentBalance(
                event.hideCurrentBalance
            )

            is SettingsEvent.SetHideIncome -> setHideIncome(
                event.hideIncome
            )

            is SettingsEvent.SetTransfersAsIncomeExpense -> setTransfersAsIncomeExpense(
                event.treatTransfersAsIncomeExpense
            )

            is SettingsEvent.SetCompactAccountsMode -> setBoolPreference(
                preference = preferenceToggles.compactAccountsMode,
                state = compactAccountsMode,
                enabled = event.enabled
            )

            is SettingsEvent.SetHideAccountTotalBalance -> setBoolPreference(
                preference = preferenceToggles.hideTotalBalance,
                state = hideAccountTotalBalance,
                enabled = event.enabled
            )

            is SettingsEvent.SetCompactCategoriesMode -> setBoolPreference(
                preference = preferenceToggles.compactCategoriesMode,
                state = compactCategoriesMode,
                enabled = event.enabled
            )

            is SettingsEvent.SetShowAccountColorsInTransactions -> setBoolPreference(
                preference = preferenceToggles.showAccountColorsInTransactions,
                state = showAccountColorsInTransactions,
                enabled = event.enabled
            )

            is SettingsEvent.SetShowTitleSuggestions -> setBoolPreference(
                preference = preferenceToggles.showTitleSuggestions,
                state = showTitleSuggestions,
                enabled = event.enabled
            )

            is SettingsEvent.SetStandardKeypadLayout -> setBoolPreference(
                preference = preferenceToggles.standardKeypadLayout,
                state = standardKeypadLayout,
                enabled = event.enabled
            )

            is SettingsEvent.SetShowCategorySearchBar -> setBoolPreference(
                preference = preferenceToggles.showCategorySearchBar,
                state = showCategorySearchBar,
                enabled = event.enabled
            )

            is SettingsEvent.SetSortCategoriesAscending -> setBoolPreference(
                preference = preferenceToggles.sortCategoriesAscending,
                state = sortCategoriesAscending,
                enabled = event.enabled
            )

            is SettingsEvent.SetShowPlannedPaymentsQuickAccess -> setBoolPreference(
                preference = preferenceToggles.showPlannedPaymentsQuickAccess,
                state = showPlannedPaymentsQuickAccess,
                enabled = event.enabled
            )

            is SettingsEvent.SetShowBudgetsQuickAccess -> setBoolPreference(
                preference = preferenceToggles.showBudgetsQuickAccess,
                state = showBudgetsQuickAccess,
                enabled = event.enabled
            )

            is SettingsEvent.SetShowLoansQuickAccess -> setBoolPreference(
                preference = preferenceToggles.showLoansQuickAccess,
                state = showLoansQuickAccess,
                enabled = event.enabled
            )

            is SettingsEvent.SetStartDateOfMonth -> setStartDateOfMonth(event.startDate)

            SettingsEvent.DeleteAllUserData -> deleteAllUserData()
            SettingsEvent.SwitchLanguage -> switchLanguage()
        }
    }

    private fun setCurrency(newCurrency: String) {
        currencyCode.value = newCurrency

        viewModelScope.launch {
            val assetCode = AssetCode.from(newCurrency).getOrNull() ?: return@launch
            setBaseCurrency(assetCode)
            syncExchangeRatesUseCase.sync(assetCode)
        }
    }

    private fun exportToCSV() {
        filePicker.createFile(
            "IvyWalletExport_${utcTimestamp()}.csv"
        ) { fileUri ->
            viewModelScope.launch {
                exportCsvUseCase.exportToFile(
                    outputFile = ExternalFile(fileUri.toString())
                )

                _uiEvents.emit(SettingsUiEvent.ShareCsvFile(fileUri))
            }
        }
    }

    private fun exportToZip() {
        filePicker.createFile(
            "IvyWalletBackup_${utcTimestamp()}.zip"
        ) { fileUri ->
            viewModelScope.launch(Dispatchers.IO) {
                progressState.value = true
                exportBackupUseCase(ExternalFile(fileUri.toString()))
                progressState.value = false

                withContext(Dispatchers.Main) {
                    _uiEvents.emit(SettingsUiEvent.ShareZipFile(fileUri))
                }
            }
        }
    }

    private fun saveGitHubBackupConfig(config: GitHubBackupConfig) {
        saveGitHubBackupConfigUseCase(config)
        gitHubBackupConfig.value = getGitHubBackupConfigUseCase()
        viewModelScope.launch {
            _uiEvents.emit(SettingsUiEvent.ShowMessage("已保存 GitHub 云备份配置"))
        }
    }

    private fun clearGitHubBackupConfig() {
        saveGitHubBackupConfigUseCase.clear()
        gitHubBackupConfig.value = null
        gitHubLastBackupEpochSec.value = null
        viewModelScope.launch {
            _uiEvents.emit(SettingsUiEvent.ShowMessage("已清除 GitHub 云备份配置"))
        }
    }

    private fun testGitHubConnection(config: GitHubBackupConfig) {
        viewModelScope.launch {
            progressState.value = true
            val result = testGitHubConnectionUseCase(config)
            progressState.value = false
            val message = result.fold(
                onSuccess = { "连接成功，仓库可访问" },
                onFailure = { it.message ?: "连接失败" }
            )
            _uiEvents.emit(SettingsUiEvent.ShowMessage(message))
        }
    }

    private fun backupToGitHub() {
        viewModelScope.launch {
            progressState.value = true
            val result = uploadBackupToGitHubUseCase()
            progressState.value = false
            val message = result.fold(
                onSuccess = {
                    gitHubLastBackupEpochSec.value = getGitHubLastBackupUseCase()
                    "已备份到 GitHub"
                },
                onFailure = { it.message ?: "备份失败" }
            )
            _uiEvents.emit(SettingsUiEvent.ShowMessage(message))
        }
    }

    private fun restoreFromGitHub() {
        viewModelScope.launch {
            progressState.value = true
            val result = restoreBackupFromGitHubUseCase()
            progressState.value = false
            result.fold(
                onSuccess = {
                    _uiEvents.emit(
                        SettingsUiEvent.ShowMessage("已从 GitHub 恢复 ${it.transactionsImported} 笔交易")
                    )
                    _uiEvents.emit(SettingsUiEvent.DataRestored)
                },
                onFailure = {
                    _uiEvents.emit(SettingsUiEvent.ShowMessage(it.message ?: "恢复失败"))
                }
            )
        }
    }

    private fun utcTimestamp(): String =
        Instant.now()
            .atZone(ZoneOffset.UTC)
            .toLocalDateTime()
            .format(exportTimestampFormatter)

    private fun switchTheme() {
        viewModelScope.launch {
            val newTheme = switchThemeUseCase()
            themeState.update(newTheme)
            currentTheme.value = newTheme
        }
    }

    private fun setLockApp(lock: Boolean) {
        lockApp.value = lock

        viewModelScope.launch {
            setAppLockEnabledPreference(lock)
        }
    }

    private fun setShowNotifications(notificationsShow: Boolean) {
        showNotifications.value = notificationsShow

        viewModelScope.launch {
            setShowNotificationsPreference(notificationsShow)
        }
    }

    private fun setHideCurrentBalance(hideBalance: Boolean) {
        hideCurrentBalance.value = hideBalance

        viewModelScope.launch {
            setHideCurrentBalancePreference(hideBalance)
        }
    }

    private fun setHideIncome(isHideIncome: Boolean) {
        hideIncome.value = isHideIncome

        viewModelScope.launch {
            setHideIncomePreference(isHideIncome)
        }
    }

    private fun setTransfersAsIncomeExpense(setTransfersAsIncomeExpense: Boolean) {
        treatTransfersAsIncomeExpense.value = setTransfersAsIncomeExpense

        viewModelScope.launch {
            setTransfersAsIncomeExpensePreference(treatTransfersAsIncomeExpense.value)
        }
    }

    private fun setBoolPreference(
        preference: BoolPreference,
        state: androidx.compose.runtime.MutableState<Boolean>,
        enabled: Boolean
    ) {
        state.value = enabled

        viewModelScope.launch {
            preferenceToggleService.set(preference, enabled)
        }
    }

    private fun setStartDateOfMonth(startDate: Int) {
        viewModelScope.launch {
            val startDay = setStartDayOfMonth(startDate) ?: return@launch
            periodState.updateStartDayOfMonth(startDay)
            periodState.initSelectedPeriod(
                startDayOfMonth = startDay,
                forceReinitialize = true
            )
            startDateOfMonth.intValue = startDay
        }
    }

    private fun deleteAllUserData() {
        viewModelScope.launch {
            resetWalletDataUseCase.resetAllData()
            _uiEvents.emit(SettingsUiEvent.WalletDataReset)
        }
    }

    private fun switchLanguage() {
        localeSettingsLauncher.openAppLocaleSettings()
    }
}
