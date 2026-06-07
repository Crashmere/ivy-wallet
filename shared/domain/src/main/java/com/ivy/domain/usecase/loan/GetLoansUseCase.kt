package com.ivy.domain.usecase.loan

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.read.LoanDao
import com.ivy.data.model.legacy.Loan
import com.ivy.domain.mapper.legacy.toLegacyDomain
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetLoansUseCase @Inject constructor(
    private val loanDao: LoanDao,
    private val dispatchers: DispatchersProvider
) {
    suspend operator fun invoke(): List<Loan> {
        return withContext(dispatchers.io) {
            loanDao.findAll().map { it.toLegacyDomain() }
        }
    }
}
