package com.ivy.data.backup

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import com.ivy.data.api.GitHubBackupConfigStore
import com.ivy.data.api.backup.BackupStore
import com.ivy.data.api.backup.GitHubBackupStore
import com.ivy.data.model.GitHubBackupConfig
import com.ivy.data.model.importing.ImportResult
import com.ivy.data.remote.GitHubBackupDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

internal class DefaultGitHubBackupStore @Inject internal constructor(
    private val dataSource: GitHubBackupDataSource,
    private val backupStore: BackupStore,
    private val configStore: GitHubBackupConfigStore,
) : GitHubBackupStore {

    override suspend fun testConnection(config: GitHubBackupConfig): Either<String, Unit> =
        dataSource.checkAccess(config)

    override suspend fun upload(): Either<String, Unit> = withContext(Dispatchers.IO) {
        val config = configStore.getGitHubBackupConfig() ?: return@withContext notConfigured()
        either {
            val json = backupStore.exportBackupJson()
            val contentBase64 = dataSource.encodeBase64(json.toByteArray(Charsets.UTF_8))
            val existing = dataSource.getFile(config).bind()
            dataSource.putFile(
                config = config,
                contentBase64 = contentBase64,
                sha = existing?.sha,
                message = commitMessage(),
            ).bind()
            configStore.gitHubLastBackupEpochSec = System.currentTimeMillis() / 1000
        }
    }

    override suspend fun restore(
        onProgress: suspend (Double) -> Unit,
    ): Either<String, ImportResult> = withContext(Dispatchers.IO) {
        val config = configStore.getGitHubBackupConfig() ?: return@withContext notConfigured()
        either {
            val remote = dataSource.getFile(config).bind()
                ?: raise("云端还没有备份文件，请先执行一次备份")
            backupStore.importBackupJson(remote.decodedContent, onProgress)
        }
    }

    private fun notConfigured(): Either<String, Nothing> = "尚未配置 GitHub 云备份".left()

    private fun commitMessage(): String {
        val timestamp = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault())
            .format(Instant.now())
        return "Ivy Wallet backup $timestamp"
    }
}
