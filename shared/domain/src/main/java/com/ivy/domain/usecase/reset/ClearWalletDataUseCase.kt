package com.ivy.domain.usecase.reset

import com.ivy.data.api.AccountStore
import com.ivy.data.api.BudgetStore
import com.ivy.data.api.CategoryStore
import com.ivy.data.api.ExchangeRateStore
import com.ivy.data.api.PlannedPaymentRuleStore
import com.ivy.data.api.TagStore
import com.ivy.data.api.TransactionStore
import com.ivy.data.db.dao.write.WriteLoanDao
import com.ivy.data.db.dao.write.WriteLoanRecordDao
import com.ivy.data.db.dao.write.WriteSettingsDao
import javax.inject.Inject

class ClearWalletDataUseCase @Inject constructor(
    private val accountStore: AccountStore,
    private val transactionRepository: TransactionStore,
    private val categoryStore: CategoryStore,
    private val tagStore: TagStore,
    private val writeSettingsDao: WriteSettingsDao,
    private val plannedPaymentRuleStore: PlannedPaymentRuleStore,
    private val budgetStore: BudgetStore,
    private val writeLoanDao: WriteLoanDao,
    private val writeLoanRecordDao: WriteLoanRecordDao,
    private val exchangeRateStore: ExchangeRateStore,
) {
    suspend operator fun invoke() {
        accountStore.deleteAll()
        transactionRepository.deleteAll()
        categoryStore.deleteAll()
        tagStore.deleteAll()
        writeSettingsDao.deleteAll()
        plannedPaymentRuleStore.deleteAll()
        budgetStore.deleteAll()
        writeLoanDao.deleteAll()
        writeLoanRecordDao.deleteAll()
        exchangeRateStore.deleteAll()
    }
}
