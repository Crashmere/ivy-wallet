package com.ivy.domain.usecase.account

import com.ivy.data.api.AccountStore
import com.ivy.data.api.PlannedPaymentRuleStore
import com.ivy.data.api.TransactionStore
import com.ivy.data.model.AccountId
import javax.inject.Inject

class DeleteAccountUseCase @Inject internal constructor(
    private val accountStore: AccountStore,
    private val transactionStore: TransactionStore,
    private val plannedPaymentRuleStore: PlannedPaymentRuleStore,
) {
    suspend operator fun invoke(accountId: AccountId) {
        transactionStore.deleteAllByAccountId(accountId)
        plannedPaymentRuleStore.deleteByAccountId(accountId = accountId.value)
        accountStore.deleteById(accountId)
    }
}
