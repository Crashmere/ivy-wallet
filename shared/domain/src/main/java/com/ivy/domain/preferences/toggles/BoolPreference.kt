package com.ivy.domain.preferences.toggles

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.ivy.data.datastore.DatastoreKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class BoolPreference(
    val key: String,
    val group: PreferenceGroup? = null,
    val name: String? = null,
    val description: String? = null,
    val defaultValue: Boolean
) {
    suspend fun isEnabled(dataStore: DataStore<Preferences>): Boolean =
        enabledFlow(dataStore).first() ?: defaultValue

    fun enabledFlow(dataStore: DataStore<Preferences>): Flow<Boolean?> = dataStore
        .data.map {
            it[preferenceKey] ?: defaultValue
        }

    suspend fun set(dataStore: DataStore<Preferences>, enabled: Boolean) {
        dataStore.edit {
            it[preferenceKey] = enabled
        }
    }

    private val preferenceKey: Preferences.Key<Boolean>
        get() = DatastoreKeys.preferenceToggle(key)
}
