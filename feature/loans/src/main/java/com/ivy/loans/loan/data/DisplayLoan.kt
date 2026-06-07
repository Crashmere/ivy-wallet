package com.ivy.loans.loan.data

import com.ivy.data.model.legacy.Loan
import com.ivy.base.currency.getDefaultFIATCurrency
import com.ivy.data.model.Reorderable

data class DisplayLoan(
    val loan: Loan,
    val loanTotalAmount: Double,
    val amountPaid: Double,
    val currencyCode: String? = getDefaultFIATCurrency().currencyCode,
    val formattedDisplayText: String = "",
    val percentPaid: Double = 0.0
) : Reorderable {
    override val orderNum: Double
        get() = loan.orderNum

    override fun withNewOrderNum(newOrderNum: Double): Reorderable {
        return this.copy(
            loan = loan.copy(
                orderNum = newOrderNum
            )
        )
    }
}
