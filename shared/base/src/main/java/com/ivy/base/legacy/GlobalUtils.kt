package com.ivy.base.legacy

import android.content.Context
import androidx.annotation.StringRes

@Deprecated("Legacy. Will be removed.")
lateinit var appContext: Context

@Deprecated("Legacy. Will be removed.")
fun stringRes(
    @StringRes id: Int,
    vararg args: String
): String {
    // I don't want strings.xml to handle something different than String at this point
    return appContext.getString(id, *args)
}
