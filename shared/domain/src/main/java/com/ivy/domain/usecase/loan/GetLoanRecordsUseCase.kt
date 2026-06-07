package com.ivy.domain.usecase.loan

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.read.LoanRecordDao
import com.ivy.legacy.domain.mapper.toLegacyDomain
import com.ivy.data.model.legacy.LoanRecord
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class GetLoanRecordsUseCase @Inject constructor(
    private val loanRecordDao: LoanRecordDao,
    private val dispatchers: DispatchersProvider
) {
    suspend operator fun invoke(loanId: UUID): List<LoanRecord> {
        return withContext(dispatchers.io) {
            loanRecordDao.findAllByLoanId(loanId = loanId)
                .map { it.toLegacyDomain() }
        }
    }
}
