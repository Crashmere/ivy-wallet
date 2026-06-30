package com.ivy.settings

import android.net.Uri

internal sealed interface SettingsUiEvent {
    data object WalletDataReset : SettingsUiEvent
    data class ShareCsvFile(val fileUri: Uri) : SettingsUiEvent
    data class ShareZipFile(val fileUri: Uri) : SettingsUiEvent
    data class ShowMessage(val message: String) : SettingsUiEvent
    data object DataRestored : SettingsUiEvent
}
