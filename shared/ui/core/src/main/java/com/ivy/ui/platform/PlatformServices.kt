package com.ivy.ui.platform

import android.net.Uri
import androidx.compose.runtime.compositionLocalOf

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

@Suppress("CompositionLocalAllowlist")
val LocalBuildInfoProvider = compositionLocalOf<BuildInfoProvider> {
    error("No LocalBuildInfoProvider")
}

@Suppress("CompositionLocalAllowlist")
val LocalFileSharer = compositionLocalOf<FileSharer> {
    error("No LocalFileSharer")
}
