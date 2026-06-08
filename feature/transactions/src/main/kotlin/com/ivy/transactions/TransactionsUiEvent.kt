package com.ivy.transactions

sealed interface TransactionsUiEvent {
    data object CloseScreen : TransactionsUiEvent
}
