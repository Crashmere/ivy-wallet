package com.ivy.domain.usecase.account

import com.ivy.data.api.AppPreferenceStore
import java.util.UUID
import javax.inject.Inject

class GetLastSelectedAccountIdUseCase @Inject constructor(
    private val appPreferences: AppPreferenceStore,
) {
    operator fun invoke(): UUID? {
        return appPreferences.lastSelectedAccountId?.let(UUID::fromString)
    }
}
