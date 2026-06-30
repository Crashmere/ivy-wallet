package com.ivy.data.api.backup

import arrow.core.Either
import com.ivy.data.model.GitHubBackupConfig
import com.ivy.data.model.importing.ImportResult

/**
 * One-way "personal cloud backup" on top of a private GitHub repository.
 *
 * [upload] pushes a full snapshot of local data; [restore] pulls the latest snapshot back. There is
 * no merge/conflict resolution: an upload overwrites the remote file (the repo's commit history is
 * the safety net), and a restore imports the remote snapshot into the local database.
 *
 * All operations return a human-readable error message on the [Either.Left] side.
 */
interface GitHubBackupStore {
    /**
     * Verifies that [config] can reach its repository (used to validate user input before saving).
     */
    suspend fun testConnection(config: GitHubBackupConfig): Either<String, Unit>

    suspend fun upload(): Either<String, Unit>

    suspend fun restore(onProgress: suspend (Double) -> Unit): Either<String, ImportResult>
}
