package com.ivy.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

typealias IvyDataStore = DataStore<Preferences>

val Context.dataStore: IvyDataStore by preferencesDataStore(
    name = "ivy_wallet_datastore_v1"
)
