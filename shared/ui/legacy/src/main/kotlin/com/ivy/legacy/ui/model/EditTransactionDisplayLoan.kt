package com.ivy.legacy.ui.model

import androidx.compose.runtime.Immutable

@Immutable
data class EditTransactionDisplayLoan(
    val isLoan: Boolean = false,
    val isLoanRecord: Boolean = false,
    val loanCaption: String? = null,
    val loanWarningDescription: String = ""
)
