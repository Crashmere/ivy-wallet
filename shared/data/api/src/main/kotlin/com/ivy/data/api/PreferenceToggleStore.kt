package com.ivy.data.api

import kotlinx.coroutines.flow.Flow

interface PreferenceToggleStore {
    suspend fun isEnabled(
        storageKey: String,
        defaultValue: Boolean,
    ): Boolean

    fun enabledFlow(
        storageKey: String,
        defaultValue: Boolean,
    ): Flow<Boolean?>

    suspend fun set(
        storageKey: String,
        enabled: Boolean,
    )

    suspend fun clearAll()
}
