package com.ivy.domain.usecase.account

import com.ivy.domain.preferences.AppPreferences
import java.util.UUID
import javax.inject.Inject

class GetLastSelectedAccountIdUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(): UUID? {
        return appPreferences.lastSelectedAccountId?.let(UUID::fromString)
    }
}
