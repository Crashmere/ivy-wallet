package com.ivy.data.api.file

import android.net.Uri
import java.nio.charset.Charset

interface TextFileStore {
    fun writeText(
        uri: Uri,
        content: String,
    ): Result<Unit>

    fun readText(
        uri: Uri,
        charset: Charset = Charsets.UTF_8,
    ): Result<String>
}
