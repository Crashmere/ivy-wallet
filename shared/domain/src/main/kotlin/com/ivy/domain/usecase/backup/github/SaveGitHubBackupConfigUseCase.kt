package com.ivy.domain.usecase.backup.github

import com.ivy.data.api.GitHubBackupConfigStore
import com.ivy.data.model.GitHubBackupConfig
import javax.inject.Inject

class SaveGitHubBackupConfigUseCase @Inject internal constructor(
    private val configStore: GitHubBackupConfigStore,
) {
    operator fun invoke(config: GitHubBackupConfig) {
        configStore.saveGitHubBackupConfig(config)
    }

    fun clear() {
        configStore.clearGitHubBackupConfig()
    }
}
