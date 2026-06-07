package com.ivy.base.resource

class TestResourceProvider : ResourceProvider {
    private val strings = mutableMapOf<Int, String>()

    fun putString(resId: Int, value: String) {
        strings[resId] = value
    }

    override fun getString(resId: Int): String {
        return strings[resId] ?: stringNotFoundError(resId)
    }

    override fun getString(resId: Int, vararg args: Any): String {
        return strings[resId]?.let { String.format(it, *args) }
            ?: stringNotFoundError(resId)
    }

    private fun stringNotFoundError(resId: Int): Nothing =
        throw TestStringNotFoundException(resId)
}

class TestStringNotFoundException(val stringRes: Int) :
    IllegalStateException("TestResourceProvider(): String not found for resId=$stringRes")
