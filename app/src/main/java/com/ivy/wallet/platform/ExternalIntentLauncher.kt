package com.ivy.wallet.platform

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity

class ExternalIntentLauncher(
    private val activity: ComponentActivity
) {
    @Suppress("TooGenericExceptionCaught", "PrintStackTrace")
    fun openUrlInBrowser(url: String) {
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW)
            browserIntent.data = Uri.parse(url)
            activity.startActivity(browserIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                activity,
                "No browser app found. Visit manually: $url",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @Suppress("SwallowedException")
    fun openGooglePlayAppPage(appId: String) {
        try {
            activity.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appId"))
            )
        } catch (e: ActivityNotFoundException) {
            activity.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$appId")
                )
            )
        }
    }

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
