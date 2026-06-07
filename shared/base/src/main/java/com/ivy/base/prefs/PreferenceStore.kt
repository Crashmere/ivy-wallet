package com.ivy.base.prefs

interface PreferenceStore {
    fun putInt(key: String, value: Int)

    fun putString(key: String, value: String?)

    fun putBoolean(key: String, value: Boolean)

    fun getInt(key: String, defValue: Int): Int

    fun getBoolean(key: String, defValue: Boolean): Boolean

    fun getString(key: String, defValue: String?): String?

    fun removeAll()
}
