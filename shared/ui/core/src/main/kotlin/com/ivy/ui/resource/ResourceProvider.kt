package com.ivy.ui.resource

interface ResourceProvider {
    fun getString(resId: Int): String
    fun getString(resId: Int, vararg args: Any): String
}
