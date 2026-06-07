package com.ivy.domain.usecase.file

import android.net.Uri
import com.ivy.data.file.FileSystem
import java.nio.charset.Charset
import javax.inject.Inject

class ReadTextFileUseCase @Inject constructor(
    private val fileSystem: FileSystem
) {
    suspend operator fun invoke(
        file: Uri,
        charset: Charset,
    ): String? {
        return fileSystem.read(file, charset).getOrNull()
    }
}
