package com.ivy.data.model

/**
 * User-provided configuration for backing up local wallet data to a private GitHub repository.
 *
 * The backup is a single JSON file (see [path]) committed to [repo]; every backup is a new commit,
 * so the repository's history doubles as the version history of the backup.
 */
data class GitHubBackupConfig(
    val token: String,
    val owner: String,
    val repo: String,
    val branch: String = DEFAULT_BRANCH,
    val path: String = DEFAULT_PATH,
) {
    companion object {
        const val DEFAULT_BRANCH = "main"
        const val DEFAULT_PATH = "ivy-wallet-backup.json"
    }
}
