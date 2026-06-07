package com.ivy.wallet.platform

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity

class ActivityFileSharer(
    private val activity: ComponentActivity
) {
    fun shareCSVFile(fileUri: Uri) {
        shareFile(
            fileUri = fileUri,
            mimeType = "text/csv"
        )
    }

    fun shareZipFile(fileUri: Uri) {
        shareFile(
            fileUri = fileUri,
            mimeType = "application/zip"
        )
    }

    private fun shareFile(
        fileUri: Uri,
        mimeType: String
    ) {
        val intent = Intent.createChooser(
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, fileUri)
                type = mimeType
            },
            null
        )
        activity.startActivity(intent)
    }
}
