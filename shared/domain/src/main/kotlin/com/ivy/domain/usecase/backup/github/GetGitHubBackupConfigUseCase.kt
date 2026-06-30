package com.ivy.domain.usecase.backup.github

import com.ivy.data.api.GitHubBackupConfigStore
import com.ivy.data.model.GitHubBackupConfig
import javax.inject.Inject

class GetGitHubBackupConfigUseCase @Inject internal constructor(
    private val configStore: GitHubBackupConfigStore,
) {
    operator fun invoke(): GitHubBackupConfig? = configStore.getGitHubBackupConfig()
}
