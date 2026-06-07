package com.ivy.domain.usecase.account

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.api.AccountStore
import com.ivy.data.db.dao.write.WritePlannedPaymentRuleDao
import com.ivy.data.model.AccountId
import com.ivy.data.api.TransactionStore
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val accountStore: AccountStore,
    private val transactionRepository: TransactionStore,
    private val plannedPaymentRuleWriter: WritePlannedPaymentRuleDao,
    private val dispatchers: DispatchersProvider
) {
    suspend operator fun invoke(accountId: AccountId) {
        withContext(dispatchers.io) {
            transactionRepository.deleteAllByAccountId(accountId)
            plannedPaymentRuleWriter.deletedByAccountId(accountId = accountId.value)
            accountStore.deleteById(accountId)
        }
    }
}
