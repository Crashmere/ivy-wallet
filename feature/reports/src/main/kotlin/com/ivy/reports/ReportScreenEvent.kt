package com.ivy.reports

import java.util.UUID

internal sealed class ReportScreenEvent {
    data class OnFilter(val filter: ReportFilter?) : ReportScreenEvent()
    data object OnExport : ReportScreenEvent()
    data class OnUpcomingExpanded(val upcomingExpanded: Boolean) : ReportScreenEvent()
    data class OnOverdueExpanded(val overdueExpanded: Boolean) : ReportScreenEvent()
    data class OnFilterOverlayVisible(val filterOverlayVisible: Boolean) : ReportScreenEvent()
    data class OnTagSearch(val data: String) : ReportScreenEvent()
    data class OnTreatTransfersAsIncomeExpense(val transfersAsIncomeExpense: Boolean) :
        ReportScreenEvent()

    data class SkipTransactions(val transactionIds: List<UUID>) :
        ReportScreenEvent()

    data class SkipTransaction(val transactionId: UUID) :
        ReportScreenEvent()

    data class OnPayOrGetTransaction(val transactionId: UUID) :
        ReportScreenEvent()
}
