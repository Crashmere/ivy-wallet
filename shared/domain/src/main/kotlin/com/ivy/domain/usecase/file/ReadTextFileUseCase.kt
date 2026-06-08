package com.ivy.domain.usecase.file

import com.ivy.data.model.ExternalFile
import com.ivy.data.api.file.TextFileStore
import java.nio.charset.Charset
import javax.inject.Inject

class ReadTextFileUseCase @Inject constructor(
    private val textFileStore: TextFileStore
) {
    suspend operator fun invoke(
        file: ExternalFile,
        charset: Charset,
    ): String? {
        return textFileStore.readText(file, charset).getOrNull()
    }
}
