package com.ivy.domain.usecase.loan

import com.ivy.data.api.LoanRecordStore
import com.ivy.data.model.legacy.LoanRecord
import java.util.UUID
import javax.inject.Inject

class GetLoanRecordsUseCase @Inject constructor(
    private val loanRecordStore: LoanRecordStore,
) {
    suspend operator fun invoke(loanId: UUID): List<LoanRecord> {
        return loanRecordStore.findAllByLoanId(loanId = loanId)
    }
}
