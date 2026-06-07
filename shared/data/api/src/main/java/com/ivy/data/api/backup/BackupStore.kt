package com.ivy.data.api.backup

import android.net.Uri
import com.ivy.data.model.importing.ImportResult

interface BackupStore {
    suspend fun exportBackup(outputFile: Uri)

    suspend fun importBackup(
        backupFile: Uri,
        onProgress: suspend (Double) -> Unit,
    ): ImportResult
}
