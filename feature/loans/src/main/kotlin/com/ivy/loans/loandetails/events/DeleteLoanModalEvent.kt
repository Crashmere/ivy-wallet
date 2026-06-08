package com.ivy.loans.loandetails.events

internal sealed interface DeleteLoanModalEvent : LoanDetailsScreenEvent {
    data object OnDeleteLoan : DeleteLoanModalEvent
    data class OnDismissDeleteLoan(val isDeleteModalVisible: Boolean) : DeleteLoanModalEvent
}
