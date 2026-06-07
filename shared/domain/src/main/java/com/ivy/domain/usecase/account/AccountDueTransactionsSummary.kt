package com.ivy.domain.usecase.account

import com.ivy.data.model.Transaction

data class AccountDueTransactionsSummary(
    val income: Double,
    val expenses: Double,
    val transactions: List<Transaction>
)
