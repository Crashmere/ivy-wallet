package com.ivy.loans.loandetails

sealed interface LoanDetailsUiEvent {
    data object CloseScreen : LoanDetailsUiEvent
}
