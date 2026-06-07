package com.ivy.domain.usecase.loan

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.write.WriteLoanDao
import com.ivy.data.model.legacy.Loan
import com.ivy.domain.mapper.legacy.toEntity
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ReorderLoansUseCase @Inject constructor(
    private val loanWriter: WriteLoanDao,
    private val dispatchers: DispatchersProvider
) {
    suspend operator fun invoke(loans: List<Loan>) {
        withContext(dispatchers.io) {
            loans.forEachIndexed { index, loan ->
                loanWriter.save(
                    loan.toEntity().copy(
                        orderNum = index.toDouble(),
                    )
                )
            }
        }
    }
}
