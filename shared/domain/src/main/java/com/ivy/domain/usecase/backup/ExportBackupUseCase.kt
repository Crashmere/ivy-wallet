package com.ivy.domain.usecase.backup

import android.net.Uri
import com.ivy.data.backup.BackupDataUseCase
import javax.inject.Inject

class ExportBackupUseCase @Inject constructor(
    private val backupDataUseCase: BackupDataUseCase
) {
    suspend operator fun invoke(outputFile: Uri) {
        backupDataUseCase.exportToFile(zipFileUri = outputFile)
    }
}
