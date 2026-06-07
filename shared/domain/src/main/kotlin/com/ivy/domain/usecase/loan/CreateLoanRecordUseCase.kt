package com.ivy.domain.usecase.loan

import com.ivy.data.api.LoanRecordStore
import com.ivy.data.model.CreateLoanRecordData
import com.ivy.data.model.legacy.LoanRecord
import java.util.UUID
import javax.inject.Inject

class CreateLoanRecordUseCase @Inject constructor(
    private val loanRecordStore: LoanRecordStore,
) {
    suspend operator fun invoke(
        loanId: UUID,
        data: CreateLoanRecordData
    ): LoanRecord? {
        val note = data.note
        if (data.amount <= 0) return null

        return try {
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

            loanRecordStore.save(loanRecord)
            loanRecord
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
