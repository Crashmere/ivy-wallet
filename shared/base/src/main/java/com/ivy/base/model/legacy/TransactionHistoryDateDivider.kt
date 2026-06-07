package com.ivy.base.model.legacy

import java.time.LocalDate

data class TransactionHistoryDateDivider(
    val date: LocalDate,
    val income: Double,
    val expenses: Double
) : TransactionHistoryItem
