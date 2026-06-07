package com.ivy.wallet.platform

import android.content.Context
import androidx.annotation.StringRes
import com.ivy.base.resource.ResourceProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@Suppress("UnnecessaryPassThroughClass")
class AndroidResourceProvider @Inject constructor(
    @ApplicationContext
    private val context: Context,
) : ResourceProvider {
    override fun getString(@StringRes resId: Int): String = context.getString(resId)

    override fun getString(
        @StringRes resId: Int,
        vararg args: Any
    ): String = context.getString(resId, args)
}
