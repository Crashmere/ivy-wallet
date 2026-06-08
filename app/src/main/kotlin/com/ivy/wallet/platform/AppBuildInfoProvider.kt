package com.ivy.wallet.platform

import com.ivy.ui.platform.BuildInfoProvider
import com.ivy.wallet.BuildConfig

internal object AppBuildInfoProvider : BuildInfoProvider {
    override val isDebug: Boolean
        get() = BuildConfig.DEBUG

    override val buildVersionName: String
        get() = BuildConfig.VERSION_NAME

    override val buildVersionCode: Int
        get() = BuildConfig.VERSION_CODE
}
