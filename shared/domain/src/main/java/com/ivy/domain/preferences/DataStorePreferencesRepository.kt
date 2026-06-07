package com.ivy.domain.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import javax.inject.Inject

class DataStorePreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    suspend fun clearAll() {
        dataStore.edit {
            it.clear()
        }
    }
}
