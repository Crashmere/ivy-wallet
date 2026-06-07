package com.ivy.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.ivy.data.api.PreferenceToggleStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DataStorePreferenceToggleStore @Inject constructor(
    @ApplicationContext context: Context
) : PreferenceToggleStore {
    private val dataStore = context.dataStore

    override suspend fun isEnabled(
        storageKey: String,
        defaultValue: Boolean,
    ): Boolean {
        return enabledFlow(
            storageKey = storageKey,
            defaultValue = defaultValue,
        ).first() ?: defaultValue
    }

    override fun enabledFlow(
        storageKey: String,
        defaultValue: Boolean,
    ): Flow<Boolean?> {
        val preferenceKey = booleanPreferencesKey(storageKey)
        return dataStore.data.map {
            it[preferenceKey] ?: defaultValue
        }
    }

    override suspend fun set(
        storageKey: String,
        enabled: Boolean,
    ) {
        val preferenceKey = booleanPreferencesKey(storageKey)
        dataStore.edit {
            it[preferenceKey] = enabled
        }
    }

    override suspend fun clearAll() {
        dataStore.edit {
            it.clear()
        }
    }
}
