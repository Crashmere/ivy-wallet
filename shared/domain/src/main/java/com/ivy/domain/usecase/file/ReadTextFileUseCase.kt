package com.ivy.domain.usecase.file

import android.net.Uri
import com.ivy.base.io.TextFileStore
import java.nio.charset.Charset
import javax.inject.Inject

class ReadTextFileUseCase @Inject constructor(
    private val textFileStore: TextFileStore
) {
    suspend operator fun invoke(
        file: Uri,
        charset: Charset,
    ): String? {
        return textFileStore.readText(file, charset).getOrNull()
    }
}
