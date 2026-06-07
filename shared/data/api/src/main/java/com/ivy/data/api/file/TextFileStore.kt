package com.ivy.data.api.file

import java.nio.charset.Charset

interface TextFileStore {
    fun writeText(
        file: ExternalFile,
        content: String,
    ): Result<Unit>

    fun readText(
        file: ExternalFile,
        charset: Charset = Charsets.UTF_8,
    ): Result<String>
}
