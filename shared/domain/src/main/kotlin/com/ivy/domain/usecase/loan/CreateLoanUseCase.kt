package com.ivy.domain.usecase.loan

import com.ivy.data.api.LoanStore
import com.ivy.data.model.legacy.CreateLoanData
import com.ivy.data.model.legacy.Loan
import com.ivy.domain.util.nextOrderNum
import javax.inject.Inject

class CreateLoanUseCase @Inject constructor(
    private val loanStore: LoanStore,
) {
    suspend operator fun invoke(data: CreateLoanData): Loan? {
        val name = data.name
        if (name.isBlank()) return null
        if (data.amount <= 0) return null

        return try {
            val loan = Loan(
                name = name.trim(),
                amount = data.amount,
                type = data.type,
                color = data.color,
                icon = data.icon,
                note = data.note,
                orderNum = loanStore.findMaxOrderNum().nextOrderNum(),
                accountId = data.account?.id,
                dateTime = data.dateTime
            )
            loanStore.save(loan)
            loan
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
