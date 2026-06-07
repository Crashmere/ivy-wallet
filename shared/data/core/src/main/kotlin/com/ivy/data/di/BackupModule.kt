package com.ivy.data.di

import com.ivy.data.api.backup.BackupStore
import com.ivy.data.backup.BackupDataUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class BackupModule {
    @Binds
    abstract fun bindBackupStore(backupDataUseCase: BackupDataUseCase): BackupStore
}
