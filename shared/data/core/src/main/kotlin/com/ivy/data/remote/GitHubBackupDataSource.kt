package com.ivy.data.remote

import android.util.Base64
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.ivy.data.model.GitHubBackupConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

/**
 * Thin wrapper around the GitHub REST "Contents" API used for the personal cloud backup.
 *
 * Only three operations are needed: verifying repo access, reading the current backup file
 * (to obtain its blob `sha`, required when overwriting) and creating/updating that file.
 */
internal class GitHubBackupDataSource @Inject internal constructor(
    private val ktorClient: dagger.Lazy<HttpClient>,
) {
    data class RemoteFile(val sha: String, val decodedContent: String)

    @Serializable
    private data class ContentResponse(
        val sha: String? = null,
        val content: String? = null,
    )

    suspend fun checkAccess(config: GitHubBackupConfig): Either<String, Unit> = try {
        val response: HttpResponse = ktorClient.get().get(repoUrl(config)) {
            githubHeaders(config.token)
        }
        when (response.status) {
            HttpStatusCode.OK -> Unit.right()
            HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden ->
                "Token 无效或权限不足（${response.status.value}）".left()

            HttpStatusCode.NotFound -> "找不到仓库或无访问权限（404）".left()
            else -> "GitHub 返回错误：${response.status.value}".left()
        }
    } catch (e: Exception) {
        (e.message ?: "网络错误").left()
    }

    suspend fun getFile(config: GitHubBackupConfig): Either<String, RemoteFile?> = try {
        val response: HttpResponse = ktorClient.get().get(contentsUrl(config)) {
            githubHeaders(config.token)
            parameter("ref", config.branch)
        }
        when (response.status) {
            HttpStatusCode.OK -> {
                val body = response.body<ContentResponse>()
                val sha = body.sha
                val content = body.content
                if (sha == null || content == null) {
                    "GitHub 响应缺少备份内容".left()
                } else {
                    RemoteFile(sha = sha, decodedContent = decodeBase64(content)).right()
                }
            }

            HttpStatusCode.NotFound -> Either.Right(null)
            HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden ->
                "Token 无效或权限不足（${response.status.value}）".left()

            else -> "读取云端备份失败：${response.status.value}".left()
        }
    } catch (e: Exception) {
        (e.message ?: "网络错误").left()
    }

    suspend fun putFile(
        config: GitHubBackupConfig,
        contentBase64: String,
        sha: String?,
        message: String,
    ): Either<String, Unit> = try {
        val requestBody = buildJsonObject {
            put("message", message)
            put("content", contentBase64)
            put("branch", config.branch)
            if (sha != null) {
                put("sha", sha)
            }
        }.toString()
        val response: HttpResponse = ktorClient.get().put(contentsUrl(config)) {
            githubHeaders(config.token)
            setBody(TextContent(requestBody, ContentType.Application.Json))
        }
        when (response.status) {
            HttpStatusCode.OK, HttpStatusCode.Created -> Unit.right()
            HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden ->
                "Token 无效或写入权限不足（${response.status.value}）".left()

            HttpStatusCode.NotFound -> "找不到仓库或分支（404）".left()
            HttpStatusCode.Conflict -> "云端已更新，请重试（409）".left()
            HttpStatusCode.UnprocessableEntity -> "请求被拒绝，请检查分支与文件路径（422）".left()
            else -> "上传失败：${response.status.value}".left()
        }
    } catch (e: Exception) {
        (e.message ?: "网络错误").left()
    }

    fun encodeBase64(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)

    private fun decodeBase64(content: String): String {
        val sanitized = content.replace("\n", "").replace("\r", "")
        return String(Base64.decode(sanitized, Base64.DEFAULT), Charsets.UTF_8)
    }

    private fun HttpRequestBuilder.githubHeaders(token: String) {
        header("Authorization", "Bearer $token")
        header("Accept", "application/vnd.github+json")
        header("X-GitHub-Api-Version", "2022-11-28")
    }

    private fun contentsUrl(config: GitHubBackupConfig): String =
        "https://api.github.com/repos/${config.owner}/${config.repo}/contents/${config.path}"

    private fun repoUrl(config: GitHubBackupConfig): String =
        "https://api.github.com/repos/${config.owner}/${config.repo}"
}
