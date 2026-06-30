package com.ivy.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ivy.data.api.AppLockPreferenceStore
import com.ivy.data.api.BackupSettingsPreferenceStore
import com.ivy.data.api.BalancePrivacyPreferenceStore
import com.ivy.data.api.CategorySortOrderStore
import com.ivy.data.api.CustomerJourneyCardStore
import com.ivy.data.api.GitHubBackupConfigStore
import com.ivy.data.api.InitialSetupStore
import com.ivy.data.api.LastSelectedAccountStore
import com.ivy.data.api.LocalPreferenceResetStore
import com.ivy.data.api.NotificationPreferenceStore
import com.ivy.data.api.StartDayOfMonthStore
import com.ivy.data.api.TransferBehaviorPreferenceStore
import com.ivy.data.model.GitHubBackupConfig
import com.ivy.data.preferences.SharedPreferenceKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore-backed implementation of the app's preference ports.
 *
 * The port API is intentionally synchronous because callers such as the app-lock and
 * secure-window lifecycle code read preferences outside of coroutines. To honor that contract
 * on top of (asynchronous) DataStore, the current values are held in an in-memory [snapshot]:
 * it is hydrated once on creation (which also triggers the SharedPreferences -> DataStore
 * migration) and then updated on every write, while the durable write is dispatched off the
 * main thread. This mirrors the previous SharedPreferences semantics (synchronous first load +
 * deferred disk write) without keeping a second key-value backend around.
 *
 * Because the in-memory [snapshot] is the single source of truth for synchronous reads, this store
 * MUST be a process-wide [Singleton]: otherwise each injection site (e.g. the separate read/write
 * use cases for the last-selected account) would cache its own snapshot and never observe each
 * other's writes within a session.
 */
@Singleton
internal class DataStorePreferenceStore @Inject internal constructor(
    @ApplicationContext context: Context,
) : AppLockPreferenceStore,
    NotificationPreferenceStore,
    BalancePrivacyPreferenceStore,
    StartDayOfMonthStore,
    TransferBehaviorPreferenceStore,
    BackupSettingsPreferenceStore,
    InitialSetupStore,
    LastSelectedAccountStore,
    CategorySortOrderStore,
    CustomerJourneyCardStore,
    GitHubBackupConfigStore,
    LocalPreferenceResetStore {

    private val dataStore = context.appPreferencesDataStore
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var snapshot: Preferences = runBlocking { dataStore.data.first() }

    private val initialSetupCompletedKey =
        booleanPreferencesKey(SharedPreferenceKeys.INITIAL_SETUP_COMPLETED)
    private val appLockEnabledKey = booleanPreferencesKey(SharedPreferenceKeys.APP_LOCK_ENABLED)
    private val startDayOfMonthKey = intPreferencesKey(SharedPreferenceKeys.START_DATE_OF_MONTH)
    private val showNotificationsKey = booleanPreferencesKey(SharedPreferenceKeys.SHOW_NOTIFICATIONS)
    private val hideCurrentBalanceKey =
        booleanPreferencesKey(SharedPreferenceKeys.HIDE_CURRENT_BALANCE)
    private val hideIncomeKey = booleanPreferencesKey(SharedPreferenceKeys.HIDE_INCOME)
    private val transfersAsIncomeExpenseKey =
        booleanPreferencesKey(SharedPreferenceKeys.TRANSFERS_AS_INCOME_EXPENSE)
    private val categorySortOrderKey = intPreferencesKey(SharedPreferenceKeys.CATEGORY_SORT_ORDER)
    private val lastSelectedAccountIdKey =
        stringPreferencesKey(SharedPreferenceKeys.LAST_SELECTED_ACCOUNT_ID)
    private val gitHubTokenKey = stringPreferencesKey(SharedPreferenceKeys.GITHUB_BACKUP_TOKEN)
    private val gitHubOwnerKey = stringPreferencesKey(SharedPreferenceKeys.GITHUB_BACKUP_OWNER)
    private val gitHubRepoKey = stringPreferencesKey(SharedPreferenceKeys.GITHUB_BACKUP_REPO)
    private val gitHubBranchKey = stringPreferencesKey(SharedPreferenceKeys.GITHUB_BACKUP_BRANCH)
    private val gitHubPathKey = stringPreferencesKey(SharedPreferenceKeys.GITHUB_BACKUP_PATH)
    private val gitHubLastBackupKey =
        longPreferencesKey(SharedPreferenceKeys.GITHUB_BACKUP_LAST_EPOCH_SEC)

    override var initialSetupCompleted: Boolean
        get() = snapshot[initialSetupCompletedKey] ?: false
        set(value) = put(initialSetupCompletedKey, value)

    override var appLockEnabled: Boolean
        get() = snapshot[appLockEnabledKey] ?: false
        set(value) = put(appLockEnabledKey, value)

    override var startDayOfMonth: Int
        get() = snapshot[startDayOfMonthKey] ?: 1
        set(value) = put(startDayOfMonthKey, value)

    override var showNotifications: Boolean
        get() = snapshot[showNotificationsKey] ?: true
        set(value) = put(showNotificationsKey, value)

    override var hideCurrentBalance: Boolean
        get() = snapshot[hideCurrentBalanceKey] ?: false
        set(value) = put(hideCurrentBalanceKey, value)

    override var hideIncome: Boolean
        get() = snapshot[hideIncomeKey] ?: false
        set(value) = put(hideIncomeKey, value)

    override var transfersAsIncomeExpense: Boolean
        get() = snapshot[transfersAsIncomeExpenseKey] ?: false
        set(value) = put(transfersAsIncomeExpenseKey, value)

    override var categorySortOrder: Int
        get() = snapshot[categorySortOrderKey] ?: 0
        set(value) = put(categorySortOrderKey, value)

    override var lastSelectedAccountId: String?
        get() = snapshot[lastSelectedAccountIdKey]
        set(value) {
            if (value == null) remove(lastSelectedAccountIdKey) else put(lastSelectedAccountIdKey, value)
        }

    override fun isCustomerJourneyCardDismissed(cardId: String): Boolean {
        return snapshot[booleanPreferencesKey(customerJourneyCardDismissedKey(cardId))] ?: false
    }

    override fun dismissCustomerJourneyCard(cardId: String) {
        put(booleanPreferencesKey(customerJourneyCardDismissedKey(cardId)), true)
    }

    override fun getGitHubBackupConfig(): GitHubBackupConfig? {
        val token = snapshot[gitHubTokenKey]?.takeIf { it.isNotBlank() } ?: return null
        val owner = snapshot[gitHubOwnerKey]?.takeIf { it.isNotBlank() } ?: return null
        val repo = snapshot[gitHubRepoKey]?.takeIf { it.isNotBlank() } ?: return null
        return GitHubBackupConfig(
            token = token,
            owner = owner,
            repo = repo,
            branch = snapshot[gitHubBranchKey]?.takeIf { it.isNotBlank() }
                ?: GitHubBackupConfig.DEFAULT_BRANCH,
            path = snapshot[gitHubPathKey]?.takeIf { it.isNotBlank() }
                ?: GitHubBackupConfig.DEFAULT_PATH,
        )
    }

    override fun saveGitHubBackupConfig(config: GitHubBackupConfig) {
        put(gitHubTokenKey, config.token)
        put(gitHubOwnerKey, config.owner)
        put(gitHubRepoKey, config.repo)
        put(gitHubBranchKey, config.branch)
        put(gitHubPathKey, config.path)
    }

    override fun clearGitHubBackupConfig() {
        remove(gitHubTokenKey)
        remove(gitHubOwnerKey)
        remove(gitHubRepoKey)
        remove(gitHubBranchKey)
        remove(gitHubPathKey)
        remove(gitHubLastBackupKey)
    }

    override var gitHubLastBackupEpochSec: Long?
        get() = snapshot[gitHubLastBackupKey]
        set(value) {
            if (value == null) remove(gitHubLastBackupKey) else put(gitHubLastBackupKey, value)
        }

    override fun clearAll() {
        snapshot = emptyPreferences()
        scope.launch { dataStore.edit { it.clear() } }
    }

    private fun <T> put(key: Preferences.Key<T>, value: T) {
        snapshot = snapshot.toMutablePreferences().apply { this[key] = value }
        scope.launch { dataStore.edit { it[key] = value } }
    }

    private fun remove(key: Preferences.Key<*>) {
        snapshot = snapshot.toMutablePreferences().apply { remove(key) }
        scope.launch { dataStore.edit { it.remove(key) } }
    }

    private fun customerJourneyCardDismissedKey(cardId: String): String {
        return "$cardId${SharedPreferenceKeys.CUSTOMER_JOURNEY_CARD_DISMISSED_SUFFIX}"
    }
}
