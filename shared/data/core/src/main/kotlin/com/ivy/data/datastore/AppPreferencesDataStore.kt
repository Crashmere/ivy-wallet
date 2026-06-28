package com.ivy.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

// Legacy SharedPreferences file that older installs used for app preferences.
private const val LEGACY_PREFS_FILENAME = "ivy_wallet_prefs"

// Single key-value store for app preferences, replacing the legacy SharedPreferences backend.
// On first access, all values from the old "ivy_wallet_prefs" file are migrated automatically
// (and then removed from SharedPreferences), so existing users keep their settings with no loss.
// Kept separate from the feature-toggle DataStore so resetting preferences never clears toggles.
internal val Context.appPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "ivy_wallet_preferences_v1",
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, LEGACY_PREFS_FILENAME))
    }
)
