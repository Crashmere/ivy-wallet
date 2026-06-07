package com.ivy.legacy.domain.data

import androidx.compose.runtime.Immutable
import com.ivy.base.model.legacy.TransactionHistoryItem
import java.time.LocalDate

@Immutable
data class TransactionHistoryDateDivider(
    val date: LocalDate,
    val income: Double,
    val expenses: Double
) : TransactionHistoryItem
