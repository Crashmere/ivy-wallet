package com.ivy.domain.usecase.settings

import com.ivy.data.api.InitialSetupStore
import javax.inject.Inject

class IsInitialSetupCompletedUseCase @Inject internal constructor(
    private val initialSetupStore: InitialSetupStore,
) {
    operator fun invoke(): Boolean {
        return initialSetupStore.initialSetupCompleted
    }
}
