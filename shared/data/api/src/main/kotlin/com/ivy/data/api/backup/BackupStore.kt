package com.ivy.data.api.backup

import com.ivy.data.model.ExternalFile
import com.ivy.data.model.importing.ImportResult

interface BackupStore {
    suspend fun exportBackup(outputFile: ExternalFile)

    suspend fun importBackup(
        backupFile: ExternalFile,
        onProgress: suspend (Double) -> Unit,
    ): ImportResult
}
