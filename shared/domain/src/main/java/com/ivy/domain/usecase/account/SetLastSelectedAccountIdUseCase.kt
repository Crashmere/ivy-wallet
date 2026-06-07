package com.ivy.domain.usecase.account

import com.ivy.domain.preferences.AppPreferences
import java.util.UUID
import javax.inject.Inject

class SetLastSelectedAccountIdUseCase @Inject constructor(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(accountId: UUID) {
        appPreferences.lastSelectedAccountId = accountId.toString()
    }
}
