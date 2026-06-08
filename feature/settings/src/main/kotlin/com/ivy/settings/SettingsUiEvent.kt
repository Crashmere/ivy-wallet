package com.ivy.settings

internal sealed interface SettingsUiEvent {
    data object WalletDataReset : SettingsUiEvent
}
