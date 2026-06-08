package com.ivy.settings

sealed interface SettingsUiEvent {
    data object WalletDataReset : SettingsUiEvent
}
