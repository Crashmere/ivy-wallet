package com.ivy.domain.usecase.backup

import android.net.Uri
import com.ivy.data.backup.BackupDataUseCase
import com.ivy.data.model.importing.ImportResult
import javax.inject.Inject

class ImportBackupUseCase @Inject constructor(
    private val backupDataUseCase: BackupDataUseCase
) {
    suspend operator fun invoke(
        backupFile: Uri,
        onProgress: suspend (Double) -> Unit,
    ): ImportResult {
        return backupDataUseCase.importBackupFile(
            backupFileUri = backupFile,
            onProgress = onProgress,
        )
    }
}
