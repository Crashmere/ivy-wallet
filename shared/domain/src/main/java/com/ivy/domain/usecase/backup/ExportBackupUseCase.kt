package com.ivy.domain.usecase.backup

import com.ivy.data.api.backup.BackupStore
import com.ivy.data.api.file.ExternalFile
import javax.inject.Inject

class ExportBackupUseCase @Inject constructor(
    private val backupStore: BackupStore
) {
    suspend operator fun invoke(outputFile: ExternalFile) {
        backupStore.exportBackup(outputFile = outputFile)
    }
}
