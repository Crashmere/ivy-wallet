package com.ivy.wallet.platform

import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher

internal fun ActivityResultFilePicker.registerActivityResultLaunchers(activity: ComponentActivity) {
    var onFileCreated: (fileUri: Uri) -> Unit = {}
    val createFileLauncher: ActivityResultLauncher<String> = activity.activityForResultLauncher(
        createIntent = { _, fileName ->
            Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/csv"
                putExtra(Intent.EXTRA_TITLE, fileName)
                putExtra(
                    DocumentsContract.EXTRA_INITIAL_URI,
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        .toURI()
                )
            }
        }
    ) { _, intent ->
        intent?.data?.also {
            onFileCreated(it)
        }
    }

    registerCreateFileLauncher { fileName, onFileCreatedCallback ->
        onFileCreated = onFileCreatedCallback
        createFileLauncher.launch(fileName)
    }

    var onFileOpened: (fileUri: Uri) -> Unit = {}
    val openFileLauncher = activity.simpleActivityForResultLauncher(
        intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
    ) { _, intent ->
        intent?.data?.also {
            onFileOpened(it)
        }
    }

    registerOpenFileLauncher { onFileOpenedCallback ->
        onFileOpened = onFileOpenedCallback
        openFileLauncher.launch(Unit)
    }
}
