package com.ivy.domain.usecase.loan

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.write.WriteLoanDao
import com.ivy.data.model.legacy.Loan
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DeleteLoanUseCase @Inject constructor(
    private val loanWriter: WriteLoanDao,
    private val dispatchers: DispatchersProvider
) {
    suspend operator fun invoke(loan: Loan): Boolean {
        return try {
            withContext(dispatchers.io) {
                loanWriter.deleteById(loan.id)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
