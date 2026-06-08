package com.ivy.domain.usecase.account

import com.ivy.data.api.LastSelectedAccountStore
import java.util.UUID
import javax.inject.Inject

class GetLastSelectedAccountIdUseCase @Inject internal constructor(
    private val lastSelectedAccountStore: LastSelectedAccountStore,
) {
    operator fun invoke(): UUID? {
        return lastSelectedAccountStore.lastSelectedAccountId?.let(UUID::fromString)
    }
}
