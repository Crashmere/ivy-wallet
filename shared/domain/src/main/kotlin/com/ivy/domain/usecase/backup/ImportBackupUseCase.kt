package com.ivy.domain.usecase.backup

import com.ivy.data.api.backup.BackupStore
import com.ivy.data.api.file.ExternalFile
import com.ivy.data.model.importing.ImportResult
import javax.inject.Inject

class ImportBackupUseCase @Inject constructor(
    private val backupStore: BackupStore
) {
    suspend operator fun invoke(
        backupFile: ExternalFile,
        onProgress: suspend (Double) -> Unit,
    ): ImportResult {
        return backupStore.importBackup(
            backupFile = backupFile,
            onProgress = onProgress,
        )
    }
}
