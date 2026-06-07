package com.ivy.domain.usecase.backup

import android.net.Uri
import com.ivy.data.api.backup.BackupStore
import javax.inject.Inject

class ExportBackupUseCase @Inject constructor(
    private val backupStore: BackupStore
) {
    suspend operator fun invoke(outputFile: Uri) {
        backupStore.exportBackup(outputFile = outputFile)
    }
}
