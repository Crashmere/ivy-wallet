package com.ivy.domain.usecase.loan

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.write.WriteLoanDao
import com.ivy.data.model.legacy.Loan
import com.ivy.legacy.domain.mapper.toEntity
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateLoanUseCase @Inject constructor(
    private val loanWriter: WriteLoanDao,
    private val dispatchers: DispatchersProvider
) {
    suspend operator fun invoke(loan: Loan): Boolean {
        if (loan.name.isBlank()) return false
        if (loan.amount <= 0.0) return false

        return try {
            withContext(dispatchers.io) {
                loanWriter.save(loan.toEntity())
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
