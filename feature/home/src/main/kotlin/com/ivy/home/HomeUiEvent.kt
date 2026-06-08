package com.ivy.home

sealed interface HomeUiEvent {
    data object OpenBalance : HomeUiEvent
    data object OpenAccountsTab : HomeUiEvent
}
