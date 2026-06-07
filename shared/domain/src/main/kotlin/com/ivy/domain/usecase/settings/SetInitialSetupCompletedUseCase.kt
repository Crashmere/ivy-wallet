package com.ivy.domain.usecase.settings

import com.ivy.data.api.InitialSetupStore
import javax.inject.Inject

class SetInitialSetupCompletedUseCase @Inject constructor(
    private val initialSetupStore: InitialSetupStore,
) {
    operator fun invoke(completed: Boolean) {
        initialSetupStore.initialSetupCompleted = completed
    }
}
