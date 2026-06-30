package com.ivy.data.api.backup

import com.ivy.data.model.ExternalFile
import com.ivy.data.model.importing.ImportResult

interface BackupStore {
    suspend fun exportBackup(outputFile: ExternalFile)

    suspend fun importBackup(
        backupFile: ExternalFile,
        onProgress: suspend (Double) -> Unit,
    ): ImportResult

    /**
     * Serializes the full local data set to the same JSON format used inside the ZIP backup,
     * but returns it in-memory (UTF-8) instead of writing a file. Used by the GitHub cloud backup.
     */
    suspend fun exportBackupJson(): String

    /**
     * Imports a backup from a raw JSON string (the counterpart of [exportBackupJson]).
     */
    suspend fun importBackupJson(
        jsonString: String,
        onProgress: suspend (Double) -> Unit,
    ): ImportResult
}
