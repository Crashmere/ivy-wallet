package com.ivy.data.di

import com.ivy.base.io.TextFileStore
import com.ivy.data.file.FileSystem
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class FileSystemModule {
    @Binds
    abstract fun bindTextFileStore(fileSystem: FileSystem): TextFileStore
}
