package com.ivy.reports
import com.ivy.data.model.legacy.LegacyTransaction

import com.ivy.data.model.Transaction
import com.ivy.ui.platform.FileSharer

sealed class ReportScreenEvent {
    data class OnFilter(val filter: ReportFilter?) : ReportScreenEvent()
    data class OnExport(val fileSharer: FileSharer) : ReportScreenEvent()
    data class OnPayOrGet(val transaction: Transaction) : ReportScreenEvent()
    data class SkipTransaction(val transaction: Transaction) : ReportScreenEvent()
    data class SkipTransactions(val transactions: List<Transaction>) : ReportScreenEvent()
    data class OnUpcomingExpanded(val upcomingExpanded: Boolean) : ReportScreenEvent()
    data class OnOverdueExpanded(val overdueExpanded: Boolean) : ReportScreenEvent()
    data class OnFilterOverlayVisible(val filterOverlayVisible: Boolean) : ReportScreenEvent()
    data class OnTagSearch(val data: String) : ReportScreenEvent()
    data class OnTreatTransfersAsIncomeExpense(val transfersAsIncomeExpense: Boolean) :
        ReportScreenEvent()

        data class SkipTransactionsLegacy(val transactions: List<LegacyTransaction>) :
        ReportScreenEvent()

        data class SkipTransactionLegacy(val transaction: LegacyTransaction) :
        ReportScreenEvent()

        data class OnPayOrGetLegacy(val transaction: LegacyTransaction) :
        ReportScreenEvent()
}
