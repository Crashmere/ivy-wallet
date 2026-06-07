package com.ivy.domain.usecase.reset

import com.ivy.data.db.dao.write.WriteBudgetDao
import com.ivy.data.db.dao.write.WriteLoanDao
import com.ivy.data.db.dao.write.WriteLoanRecordDao
import com.ivy.data.db.dao.write.WritePlannedPaymentRuleDao
import com.ivy.data.db.dao.write.WriteSettingsDao
import com.ivy.data.repository.AccountRepository
import com.ivy.data.repository.CategoryRepository
import com.ivy.data.repository.ExchangeRatesRepository
import com.ivy.data.repository.TagRepository
import com.ivy.data.repository.TransactionRepository
import javax.inject.Inject

class ClearWalletDataUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val tagRepository: TagRepository,
    private val writeSettingsDao: WriteSettingsDao,
    private val writePlannedPaymentRuleDao: WritePlannedPaymentRuleDao,
    private val writeBudgetDao: WriteBudgetDao,
    private val writeLoanDao: WriteLoanDao,
    private val writeLoanRecordDao: WriteLoanRecordDao,
    private val exchangeRatesRepository: ExchangeRatesRepository,
) {
    suspend operator fun invoke() {
        accountRepository.deleteAll()
        transactionRepository.deleteAll()
        categoryRepository.deleteAll()
        tagRepository.deleteAll()
        writeSettingsDao.deleteAll()
        writePlannedPaymentRuleDao.deleteAll()
        writeBudgetDao.deleteAll()
        writeLoanDao.deleteAll()
        writeLoanRecordDao.deleteAll()
        exchangeRatesRepository.deleteAll()
    }
}
