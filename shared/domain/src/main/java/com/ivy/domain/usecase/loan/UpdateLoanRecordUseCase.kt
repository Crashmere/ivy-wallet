package com.ivy.domain.usecase.loan

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.write.WriteLoanRecordDao
import com.ivy.data.model.legacy.LoanRecord
import com.ivy.domain.mapper.legacy.toEntity
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateLoanRecordUseCase @Inject constructor(
    private val loanRecordWriter: WriteLoanRecordDao,
    private val dispatchers: DispatchersProvider
) {
    suspend operator fun invoke(loanRecord: LoanRecord): Boolean {
        if (loanRecord.amount <= 0.0) return false

        return try {
            withContext(dispatchers.io) {
                loanRecordWriter.save(loanRecord.toEntity())
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
