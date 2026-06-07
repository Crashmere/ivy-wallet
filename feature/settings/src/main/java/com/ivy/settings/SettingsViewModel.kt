package com.ivy.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewModelScope
import com.ivy.base.theme.Theme
import com.ivy.base.time.TimeProvider
import com.ivy.data.model.primitive.AssetCode
import com.ivy.domain.preferences.AppPreferences
import com.ivy.domain.preferences.toggles.BoolPreference
import com.ivy.domain.preferences.toggles.PreferenceToggles
import com.ivy.domain.usecase.ResetWalletDataUseCase
import com.ivy.domain.usecase.backup.ExportBackupUseCase
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.domain.usecase.currency.SetBaseCurrencyUseCase
import com.ivy.domain.usecase.csv.ExportCsvUseCase
import com.ivy.domain.usecase.exchange.SyncExchangeRatesUseCase
import com.ivy.domain.usecase.settings.GetThemeUseCase
import com.ivy.domain.usecase.settings.SwitchThemeUseCase
import com.ivy.legacy.frp.monad.Res
import com.ivy.ui.theme.ThemeState
import com.ivy.legacy.ui.state.PeriodState
import com.ivy.base.time.getISOFormattedDateTime
import com.ivy.base.coroutines.uiThread
import com.ivy.ui.ComposeViewModel
import com.ivy.ui.platform.FilePicker
import com.ivy.ui.platform.FileSharer
import com.ivy.ui.platform.LocaleSettingsLauncher
import com.ivy.legacy.domain.action.global.StartDayOfMonthAct
import com.ivy.legacy.domain.action.global.UpdateStartDayOfMonthAct
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZoneOffset
import javax.inject.Inject

@Stable
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeState: ThemeState,
    private val periodState: PeriodState,
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val setBaseCurrency: SetBaseCurrencyUseCase,
    private val getTheme: GetThemeUseCase,
    private val switchThemeUseCase: SwitchThemeUseCase,
    private val resetWalletDataUseCase: ResetWalletDataUseCase,
    private val appPreferences: AppPreferences,
    private val exportBackupUseCase: ExportBackupUseCase,
    private val startDayOfMonthAct: StartDayOfMonthAct,
    private val updateStartDayOfMonthAct: UpdateStartDayOfMonthAct,
    private val syncExchangeRatesUseCase: SyncExchangeRatesUseCase,
    private val preferenceToggles: PreferenceToggles,
    private val preferenceDataStore: DataStore<Preferences>,
    private val exportCsvUseCase: ExportCsvUseCase,
    private val filePicker: FilePicker,
    private val timeProvider: TimeProvider,
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
    private val startDateOfMonth = mutableIntStateOf(1)
    private val progressState = mutableStateOf(false)

    @Composable
    override fun uiState(): SettingsState {
        LaunchedEffect(Unit) {
            onStart()
        }

        return SettingsState(
            currencyCode = getCurrencyCode(),
            currentTheme = getCurrentTheme(),
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
    }

    private suspend fun initializeCurrency() {
        currencyCode.value = getBaseCurrencyCode()
    }

    private suspend fun initializeCurrentTheme() {
        currentTheme.value = getTheme()
    }

    private fun initializeLockApp() {
        lockApp.value = appPreferences.appLockEnabled
    }

    private fun initializeShowNotifications() {
        showNotifications.value = appPreferences.showNotifications
    }

    private fun initializeHideCurrentBalance() {
        hideCurrentBalance.value = appPreferences.hideCurrentBalance
    }

    private fun initializeHideIncome() {
        hideIncome.value = appPreferences.hideIncome
    }

    private fun initializeTransfersAsIncomeExpense() {
        treatTransfersAsIncomeExpense.value = appPreferences.transfersAsIncomeExpense
    }

    private suspend fun initializeTogglePreferences() {
        compactAccountsMode.value = preferenceToggles.compactAccountsMode.isEnabled(preferenceDataStore)
        hideAccountTotalBalance.value = preferenceToggles.hideTotalBalance.isEnabled(preferenceDataStore)
        compactCategoriesMode.value = preferenceToggles.compactCategoriesMode.isEnabled(preferenceDataStore)
        showAccountColorsInTransactions.value =
            preferenceToggles.showAccountColorsInTransactions.isEnabled(preferenceDataStore)
        showTitleSuggestions.value = preferenceToggles.showTitleSuggestions.isEnabled(preferenceDataStore)
        standardKeypadLayout.value = preferenceToggles.standardKeypadLayout.isEnabled(preferenceDataStore)
        showCategorySearchBar.value = preferenceToggles.showCategorySearchBar.isEnabled(preferenceDataStore)
        sortCategoriesAscending.value = preferenceToggles.sortCategoriesAscending.isEnabled(preferenceDataStore)
    }

    private suspend fun initializeStartDateOfMonth() {
        val startDay = startDayOfMonthAct(Unit)
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
            is SettingsEvent.ExportToCsv -> exportToCSV(event.fileSharer)
            is SettingsEvent.BackupData -> exportToZip(event.fileSharer)
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

    private fun exportToCSV(fileSharer: FileSharer) {
        filePicker.createFile(
            "IvyWalletExport_${utcTimestamp()}.csv"
        ) { fileUri ->
            viewModelScope.launch {
                exportCsvUseCase.exportToFile(
                    outputFile = fileUri
                )

                fileSharer.shareCSVFile(
                    fileUri = fileUri
                )
            }
        }
    }

    private fun exportToZip(fileSharer: FileSharer) {
        filePicker.createFile(
            "IvyWalletBackup_${utcTimestamp()}.zip"
        ) { fileUri ->
            viewModelScope.launch(Dispatchers.IO) {
                progressState.value = true
                exportBackupUseCase(fileUri)
                progressState.value = false

                appPreferences.dataBackupCompleted = true

                uiThread {
                    fileSharer.shareZipFile(
                        fileUri = fileUri
                    )
                }
            }
        }
    }

    private fun utcTimestamp(): String =
        timeProvider.utcNow()
            .atZone(ZoneOffset.UTC)
            .toLocalDateTime()
            .getISOFormattedDateTime()

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
            appPreferences.appLockEnabled = lock
        }
    }

    private fun setShowNotifications(notificationsShow: Boolean) {
        showNotifications.value = notificationsShow

        viewModelScope.launch {
            appPreferences.showNotifications = notificationsShow
        }
    }

    private fun setHideCurrentBalance(hideBalance: Boolean) {
        hideCurrentBalance.value = hideBalance

        viewModelScope.launch {
            appPreferences.hideCurrentBalance = hideBalance
        }
    }

    private fun setHideIncome(isHideIncome: Boolean) {
        hideIncome.value = isHideIncome

        viewModelScope.launch {
            appPreferences.hideIncome = isHideIncome
        }
    }

    private fun setTransfersAsIncomeExpense(setTransfersAsIncomeExpense: Boolean) {
        treatTransfersAsIncomeExpense.value = setTransfersAsIncomeExpense

        viewModelScope.launch {
            appPreferences.transfersAsIncomeExpense = treatTransfersAsIncomeExpense.value
        }
    }

    private fun setBoolPreference(
        preference: BoolPreference,
        state: androidx.compose.runtime.MutableState<Boolean>,
        enabled: Boolean
    ) {
        state.value = enabled

        viewModelScope.launch {
            preference.set(preferenceDataStore, enabled)
        }
    }

    private fun setStartDateOfMonth(startDate: Int) {
        viewModelScope.launch {
            when (val res = updateStartDayOfMonthAct(startDate)) {
                is Res.Err -> {}
                is Res.Ok -> {
                    val startDay = res.data
                    periodState.updateStartDayOfMonth(startDay)
                    periodState.initSelectedPeriod(
                        startDayOfMonth = startDay,
                        forceReinitialize = true
                    )
                    startDateOfMonth.intValue = startDay
                }
            }
        }
    }

    private fun deleteAllUserData() {
        viewModelScope.launch {
            logout()
        }
    }

    private fun logout() {
        viewModelScope.launch {
            resetWalletDataUseCase.resetAllData()
        }
    }

    private fun switchLanguage() {
        localeSettingsLauncher.openAppLocaleSettings()
    }
}
