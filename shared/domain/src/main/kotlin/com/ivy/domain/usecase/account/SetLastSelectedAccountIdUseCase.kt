package com.ivy.domain.usecase.account

import com.ivy.data.api.LastSelectedAccountStore
import java.util.UUID
import javax.inject.Inject

class SetLastSelectedAccountIdUseCase @Inject constructor(
    private val lastSelectedAccountStore: LastSelectedAccountStore,
) {
    operator fun invoke(accountId: UUID) {
        lastSelectedAccountStore.lastSelectedAccountId = accountId.toString()
    }
}
