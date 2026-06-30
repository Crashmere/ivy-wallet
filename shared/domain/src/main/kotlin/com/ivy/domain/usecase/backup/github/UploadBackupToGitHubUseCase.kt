package com.ivy.domain.usecase.backup.github

import com.ivy.data.api.backup.GitHubBackupStore
import javax.inject.Inject

class UploadBackupToGitHubUseCase @Inject internal constructor(
    private val store: GitHubBackupStore,
) {
    suspend operator fun invoke(): Result<Unit> = store.upload().fold(
        ifLeft = { Result.failure(GitHubBackupException(it)) },
        ifRight = { Result.success(it) },
    )
}
