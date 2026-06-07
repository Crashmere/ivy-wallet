package com.ivy.domain.preferences.toggles

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PreferenceToggleRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    suspend fun isEnabled(preference: BoolPreference): Boolean {
        return preference.isEnabled(dataStore)
    }

    fun enabledFlow(preference: BoolPreference): Flow<Boolean?> {
        return preference.enabledFlow(dataStore)
    }

    suspend fun set(
        preference: BoolPreference,
        enabled: Boolean,
    ) {
        preference.set(dataStore, enabled)
    }
}
