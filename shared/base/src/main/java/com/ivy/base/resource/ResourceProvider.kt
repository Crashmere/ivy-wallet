package com.ivy.base.resource

interface ResourceProvider {
    fun getString(resId: Int): String
    fun getString(resId: Int, vararg args: Any): String
}
