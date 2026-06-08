package com.ivy.domain.preferences.toggles

import com.ivy.data.api.PreferenceToggleStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PreferenceToggleService @Inject internal constructor(
    private val store: PreferenceToggleStore
) {
    suspend fun isEnabled(preference: BoolPreference): Boolean {
        return store.isEnabled(
            storageKey = preference.storageKey,
            defaultValue = preference.defaultValue,
        )
    }

    fun enabledFlow(preference: BoolPreference): Flow<Boolean?> {
        return store.enabledFlow(
            storageKey = preference.storageKey,
            defaultValue = preference.defaultValue,
        )
    }

    suspend fun set(
        preference: BoolPreference,
        enabled: Boolean,
    ) {
        store.set(
            storageKey = preference.storageKey,
            enabled = enabled,
        )
    }

    suspend fun clearAll() {
        store.clearAll()
    }
}
