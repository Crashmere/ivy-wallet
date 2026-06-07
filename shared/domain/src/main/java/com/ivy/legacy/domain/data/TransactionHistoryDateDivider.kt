package com.ivy.legacy.domain.data

import com.ivy.base.model.legacy.TransactionHistoryItem
import java.time.LocalDate

data class TransactionHistoryDateDivider(
    val date: LocalDate,
    val income: Double,
    val expenses: Double
) : TransactionHistoryItem
