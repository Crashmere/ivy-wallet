package com.ivy.transaction

internal sealed interface EditTransactionUiEvent {
    data object CloseScreen : EditTransactionUiEvent
}
