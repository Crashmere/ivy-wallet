package com.ivy.base.prefs

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SharedPrefs @Inject constructor(
    @ApplicationContext
    appContext: Context
) : PreferenceStore {
    companion object {
        private const val PREFS_FILENAME = "ivy_wallet_prefs"
    }

    private val preferences = appContext.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)

    override fun putInt(key: String, value: Int) {
        val editor = preferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    override fun putString(key: String, value: String?) {
        val editor = preferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    override fun putBoolean(key: String, value: Boolean) {
        val editor = preferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    override fun getInt(key: String, defValue: Int): Int {
        return preferences.getInt(key, defValue)
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return preferences.getBoolean(key, defValue)
    }

    override fun getString(key: String, defValue: String?): String? {
        return preferences.getString(key, defValue)
    }

    override fun removeAll() {
        preferences.edit().clear().apply()
    }
}
