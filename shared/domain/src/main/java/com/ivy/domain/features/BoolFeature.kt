package com.ivy.domain.features

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.ivy.data.datastore.DatastoreKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Immutable
class BoolFeature(
    val key: String,
    val group: FeatureGroup? = null,
    val name: String? = null,
    val description: String? = null,
    private val defaultValue: Boolean
) {
    @Composable
    fun asEnabledState(): Boolean {
        val dataStore = LocalFeatureDataStore.current
        val featureFlag = remember(dataStore) { enabledFlow(dataStore) }
            .collectAsState(defaultValue).value
        return featureFlag ?: defaultValue
    }

    suspend fun isEnabled(dataStore: DataStore<Preferences>): Boolean =
        enabledFlow(dataStore).first() ?: defaultValue

    fun enabledFlow(dataStore: DataStore<Preferences>): Flow<Boolean?> = dataStore
        .data.map {
            it[featureKey] ?: defaultValue
        }

    suspend fun set(dataStore: DataStore<Preferences>, enabled: Boolean) {
        dataStore.edit {
            it[featureKey] = enabled
        }
    }

    private val featureKey: Preferences.Key<Boolean>
        get() = DatastoreKeys.ivyFeature(key)
}
