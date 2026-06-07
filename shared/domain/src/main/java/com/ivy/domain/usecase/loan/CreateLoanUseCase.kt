package com.ivy.domain.usecase.loan

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.read.LoanDao
import com.ivy.data.db.dao.write.WriteLoanDao
import com.ivy.data.model.legacy.CreateLoanData
import com.ivy.data.model.legacy.Loan
import com.ivy.legacy.domain.mapper.toEntity
import com.ivy.legacy.domain.pure.util.nextOrderNum
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CreateLoanUseCase @Inject constructor(
    private val loanDao: LoanDao,
    private val loanWriter: WriteLoanDao,
    private val dispatchers: DispatchersProvider
) {
    suspend operator fun invoke(data: CreateLoanData): Loan? {
        val name = data.name
        if (name.isBlank()) return null
        if (data.amount <= 0) return null

        return try {
            withContext(dispatchers.io) {
                val loan = Loan(
                    name = name.trim(),
                    amount = data.amount,
                    type = data.type,
                    color = data.color,
                    icon = data.icon,
                    note = data.note,
                    orderNum = loanDao.findMaxOrderNum().nextOrderNum(),
                    accountId = data.account?.id,
                    dateTime = data.dateTime
                )
                loanWriter.save(loan.toEntity())
                loan
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
