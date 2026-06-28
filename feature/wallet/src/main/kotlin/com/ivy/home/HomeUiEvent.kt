package com.ivy.home

internal sealed interface HomeUiEvent {
    data object OpenBalance : HomeUiEvent
    data object OpenAccountsTab : HomeUiEvent
}
