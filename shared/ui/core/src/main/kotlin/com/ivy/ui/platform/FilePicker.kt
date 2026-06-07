package com.ivy.ui.platform

import android.net.Uri

interface FilePicker {
    fun createFile(
        fileName: String,
        onCreated: (Uri) -> Unit
    )

    fun openFile(
        onOpened: (Uri) -> Unit
    )
}
