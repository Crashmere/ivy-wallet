package com.ivy.domain.usecase.backup.github

import com.ivy.data.api.backup.GitHubBackupStore
import com.ivy.data.model.importing.ImportResult
import javax.inject.Inject

class RestoreBackupFromGitHubUseCase @Inject internal constructor(
    private val store: GitHubBackupStore,
) {
    suspend operator fun invoke(
        onProgress: suspend (Double) -> Unit = {},
    ): Result<ImportResult> = store.restore(onProgress).fold(
        ifLeft = { Result.failure(GitHubBackupException(it)) },
        ifRight = { Result.success(it) },
    )
}
