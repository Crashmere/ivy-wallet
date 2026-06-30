package com.ivy.domain.usecase.backup.github

import com.ivy.data.api.GitHubBackupConfigStore
import javax.inject.Inject

class GetGitHubLastBackupUseCase @Inject internal constructor(
    private val configStore: GitHubBackupConfigStore,
) {
    /** Epoch seconds of the last successful upload, or `null` if it never happened. */
    operator fun invoke(): Long? = configStore.gitHubLastBackupEpochSec
}
