package com.ivy.transaction

sealed interface EditTransactionUiEvent {
    data object CloseScreen : EditTransactionUiEvent
}
