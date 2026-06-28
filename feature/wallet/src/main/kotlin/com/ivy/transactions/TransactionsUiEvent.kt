package com.ivy.transactions

internal sealed interface TransactionsUiEvent {
    data object CloseScreen : TransactionsUiEvent
}
