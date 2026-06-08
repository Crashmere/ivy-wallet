package com.ivy.loans.model

import com.ivy.data.model.Loan
import com.ivy.data.model.currency.getDefaultFIATCurrency
import com.ivy.legacy.ui.component.ReorderableItem

internal data class DisplayLoan(
    val loan: Loan,
    val loanTotalAmount: Double,
    val amountPaid: Double,
    val currencyCode: String? = getDefaultFIATCurrency().currencyCode,
    val formattedDisplayText: String = "",
    val percentPaid: Double = 0.0
) : ReorderableItem {
    override val orderNum: Double
        get() = loan.orderNum

    override fun withNewOrderNum(newOrderNum: Double): ReorderableItem {
        return this.copy(
            loan = loan.copy(
                orderNum = newOrderNum
            )
        )
    }
}
