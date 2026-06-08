package com.ivy.loans.loan

import com.ivy.loans.loan.data.DisplayLoan
import com.ivy.data.model.CreateAccountData
import com.ivy.data.model.CreateLoanData

internal sealed interface LoanScreenEvent {
    data class OnLoanCreate(val createLoanData: CreateLoanData) : LoanScreenEvent
    data class OnReordered(val reorderedList: List<DisplayLoan>) : LoanScreenEvent
    data class OnCreateAccount(val accountData: CreateAccountData) : LoanScreenEvent
    data class OnReOrderModalShow(val show: Boolean) : LoanScreenEvent
    data class OnTabChanged(val tab: LoanTab) : LoanScreenEvent
    data object OnAddLoan : LoanScreenEvent
    data object OnLoanModalDismiss : LoanScreenEvent
    data object OnChangeDate : LoanScreenEvent
    data object OnChangeTime : LoanScreenEvent

    /** Toggles paid off loans visibility */
    data object OnTogglePaidOffLoanVisibility : LoanScreenEvent
}
