package com.ivy.domain.usecase.backup.github

import com.ivy.data.api.backup.GitHubBackupStore
import com.ivy.data.model.GitHubBackupConfig
import javax.inject.Inject

class TestGitHubConnectionUseCase @Inject internal constructor(
    private val store: GitHubBackupStore,
) {
    suspend operator fun invoke(config: GitHubBackupConfig): Result<Unit> =
        store.testConnection(config).fold(
            ifLeft = { Result.failure(GitHubBackupException(it)) },
            ifRight = { Result.success(it) },
        )
}
