package com.ivy.loans.model

import com.ivy.data.model.Loan
import com.ivy.data.model.currency.getDefaultFIATCurrency

internal data class DisplayLoan(
    val loan: Loan,
    val loanTotalAmount: Double,
    val amountPaid: Double,
    val currencyCode: String? = getDefaultFIATCurrency().currencyCode,
    val formattedDisplayText: String = "",
    val percentPaid: Double = 0.0
)
