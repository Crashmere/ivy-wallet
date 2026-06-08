package com.ivy.reports

import com.ivy.ui.platform.FileSharer
import java.util.UUID

internal sealed class ReportScreenEvent {
    data class OnFilter(val filter: ReportFilter?) : ReportScreenEvent()
    data class OnExport(val fileSharer: FileSharer) : ReportScreenEvent()
    data class OnUpcomingExpanded(val upcomingExpanded: Boolean) : ReportScreenEvent()
    data class OnOverdueExpanded(val overdueExpanded: Boolean) : ReportScreenEvent()
    data class OnFilterOverlayVisible(val filterOverlayVisible: Boolean) : ReportScreenEvent()
    data class OnTagSearch(val data: String) : ReportScreenEvent()
    data class OnTreatTransfersAsIncomeExpense(val transfersAsIncomeExpense: Boolean) :
        ReportScreenEvent()

    data class SkipLegacyTransactions(val transactionIds: List<UUID>) :
        ReportScreenEvent()

    data class SkipLegacyTransaction(val transactionId: UUID) :
        ReportScreenEvent()

    data class OnPayOrGetLegacyTransaction(val transactionId: UUID) :
        ReportScreenEvent()
}
