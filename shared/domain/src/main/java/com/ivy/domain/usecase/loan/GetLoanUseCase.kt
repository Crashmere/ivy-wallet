package com.ivy.domain.usecase.loan

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.read.LoanDao
import com.ivy.domain.mapper.legacy.toLegacyDomain
import com.ivy.data.model.legacy.Loan
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class GetLoanUseCase @Inject constructor(
    private val loanDao: LoanDao,
    private val dispatchers: DispatchersProvider
) {
    suspend operator fun invoke(loanId: UUID): Loan? {
        return withContext(dispatchers.io) {
            loanDao.findById(loanId)?.toLegacyDomain()
        }
    }
}
