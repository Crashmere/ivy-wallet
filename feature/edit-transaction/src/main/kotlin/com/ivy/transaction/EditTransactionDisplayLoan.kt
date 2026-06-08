package com.ivy.transaction

import androidx.compose.runtime.Immutable

@Immutable
internal data class EditTransactionDisplayLoan(
    val isLoan: Boolean = false,
    val isLoanRecord: Boolean = false,
    val loanCaption: String? = null,
    val loanWarningDescription: String = ""
)
