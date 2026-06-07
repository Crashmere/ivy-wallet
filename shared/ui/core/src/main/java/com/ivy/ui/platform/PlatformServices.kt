package com.ivy.ui.platform

import android.net.Uri

interface BuildInfoProvider {
    /**
     * BuildConfig.DEBUG
     */
    val isDebug: Boolean

    /**
     * BuildConfig.VERSION_NAME
     */
    val buildVersionName: String

    /**
     * BuildConfig.VERSION_CODE
     */
    val buildVersionCode: Int
}

interface FileSharer {
    fun shareCSVFile(fileUri: Uri)

    fun shareZipFile(fileUri: Uri)
}
