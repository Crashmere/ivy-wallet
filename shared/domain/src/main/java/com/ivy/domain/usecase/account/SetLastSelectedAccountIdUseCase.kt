package com.ivy.domain.usecase.account

import com.ivy.data.api.AppPreferenceStore
import java.util.UUID
import javax.inject.Inject

class SetLastSelectedAccountIdUseCase @Inject constructor(
    private val appPreferences: AppPreferenceStore,
) {
    operator fun invoke(accountId: UUID) {
        appPreferences.lastSelectedAccountId = accountId.toString()
    }
}
