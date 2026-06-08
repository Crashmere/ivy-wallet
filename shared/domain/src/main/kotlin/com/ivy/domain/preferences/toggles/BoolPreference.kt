package com.ivy.domain.preferences.toggles

class BoolPreference(
    val key: String,
    val name: String? = null,
    val description: String? = null,
    val defaultValue: Boolean
) {
    val storageKey: String
        get() = "feature_$key"
}
