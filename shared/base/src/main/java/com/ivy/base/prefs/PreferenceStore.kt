package com.ivy.base.prefs

interface PreferenceStore {
    fun has(key: String): Boolean

    val all: Map<String, *>

    fun putInt(key: String, value: Int)

    fun putFloat(key: String, value: Float)

    fun putDouble(key: String, value: Double)

    fun putLong(key: String, value: Long)

    fun putString(key: String, value: String?)

    fun putBoolean(key: String, value: Boolean)

    fun getLong(key: String, defValue: Long): Long

    fun getInt(key: String, defValue: Int): Int

    fun getFloat(key: String, defValue: Float): Float

    fun getBoolean(key: String, defValue: Boolean): Boolean

    fun getString(key: String): String

    fun getString(key: String, defValue: String?): String?

    fun remove(key: String)

    fun removeAll()
}
