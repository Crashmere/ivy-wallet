package com.ivy.base.legacy

fun String?.isNotNullOrBlank(): Boolean {
    return this != null && this.isNotBlank()
}
