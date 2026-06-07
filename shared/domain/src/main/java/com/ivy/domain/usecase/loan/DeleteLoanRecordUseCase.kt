package com.ivy.domain.usecase.loan

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.write.WriteLoanRecordDao
import com.ivy.data.model.legacy.LoanRecord
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DeleteLoanRecordUseCase @Inject constructor(
    private val loanRecordWriter: WriteLoanRecordDao,
    private val dispatchers: DispatchersProvider
) {
    suspend operator fun invoke(loanRecord: LoanRecord): Boolean {
        return try {
            withContext(dispatchers.io) {
                loanRecordWriter.deleteById(loanRecord.id)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
