package com.ivy.domain.usecase.loan

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.write.WriteLoanRecordDao
import com.ivy.data.model.legacy.CreateLoanRecordData
import com.ivy.data.model.legacy.LoanRecord
import com.ivy.domain.mapper.legacy.toEntity
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class CreateLoanRecordUseCase @Inject constructor(
    private val loanRecordWriter: WriteLoanRecordDao,
    private val dispatchers: DispatchersProvider
) {
    suspend operator fun invoke(
        loanId: UUID,
        data: CreateLoanRecordData
    ): LoanRecord? {
        val note = data.note
        if (data.amount <= 0) return null

        return try {
            withContext(dispatchers.io) {
                val loanRecord = LoanRecord(
                    loanId = loanId,
                    note = note?.trim(),
                    amount = data.amount,
                    dateTime = data.dateTime,
                    interest = data.interest,
                    accountId = data.account?.id,
                    convertedAmount = data.convertedAmount,
                    loanRecordType = data.loanRecordType
                )

                loanRecordWriter.save(loanRecord.toEntity())
                loanRecord
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
