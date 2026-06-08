package com.ivy.wallet.platform

import android.net.Uri
import com.ivy.ui.platform.FilePicker
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ActivityResultFilePicker @Inject constructor() : FilePicker {
    private var createFileLauncher: ((String, (Uri) -> Unit) -> Unit)? = null
    private var openFileLauncher: (((Uri) -> Unit) -> Unit)? = null

    internal fun registerCreateFileLauncher(
        launcher: (String, (Uri) -> Unit) -> Unit
    ) {
        createFileLauncher = launcher
    }

    internal fun registerOpenFileLauncher(
        launcher: ((Uri) -> Unit) -> Unit
    ) {
        openFileLauncher = launcher
    }

    override fun createFile(
        fileName: String,
        onCreated: (Uri) -> Unit
    ) {
        val launcher = createFileLauncher ?: error("File picker is not registered")
        launcher(fileName, onCreated)
    }

    override fun openFile(
        onOpened: (Uri) -> Unit
    ) {
        val launcher = openFileLauncher ?: error("File picker is not registered")
        launcher(onOpened)
    }
}
