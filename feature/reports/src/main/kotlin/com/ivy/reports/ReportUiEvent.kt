package com.ivy.reports

import android.net.Uri

internal sealed interface ReportUiEvent {
    data class ShareCsvFile(val fileUri: Uri) : ReportUiEvent
}
