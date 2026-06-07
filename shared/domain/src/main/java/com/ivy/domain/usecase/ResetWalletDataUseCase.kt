package com.ivy.domain.usecase

interface ResetWalletDataUseCase {
    suspend fun resetAllData()

    suspend fun resetCloudUserData()
}
