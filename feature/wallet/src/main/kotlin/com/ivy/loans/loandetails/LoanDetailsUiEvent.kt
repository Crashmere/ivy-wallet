package com.ivy.loans.loandetails

internal sealed interface LoanDetailsUiEvent {
    data object CloseScreen : LoanDetailsUiEvent
}
