package com.ivy.data.api

import com.ivy.data.model.GitHubBackupConfig

/**
 * Persists the [GitHubBackupConfig] entered by the user plus the timestamp of the last successful
 * upload. Reads are synchronous to match the rest of the preference ports.
 */
interface GitHubBackupConfigStore {
    /**
     * Returns the stored config, or `null` when the mandatory fields (token/owner/repo) are missing.
     */
    fun getGitHubBackupConfig(): GitHubBackupConfig?

    fun saveGitHubBackupConfig(config: GitHubBackupConfig)

    fun clearGitHubBackupConfig()

    var gitHubLastBackupEpochSec: Long?
}
