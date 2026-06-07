package com.ivy.base.text

import java.util.Locale

fun String?.isNotNullOrBlank(): Boolean {
    return this != null && this.isNotBlank()
}

fun String.toUpperCaseLocal() = this.uppercase(Locale.getDefault())

fun String.toLowerCaseLocal() = this.lowercase(Locale.getDefault())

fun String.uppercaseLocal(): String = this.uppercase(Locale.getDefault())

fun String.capitalizeLocal(): String = this.replaceFirstChar {
    if (it.isLowerCase()) {
        it.titlecase(
            Locale.getDefault()
        )
    } else {
        it.toString()
    }
}

fun String.capitalizeWords(): String {
    return split(" ").joinToString(" ") { it.capitalizeLocal() }
}
