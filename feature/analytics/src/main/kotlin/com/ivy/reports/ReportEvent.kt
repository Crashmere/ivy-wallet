package com.ivy.reports

import com.ivy.ui.period.TimePeriod
import java.util.UUID

internal sealed interface ReportEvent {
    data object OnPreviousMonth : ReportEvent
    data object OnNextMonth : ReportEvent
    data class OnSelectPeriod(val period: TimePeriod) : ReportEvent

    data object ToggleIncome : ReportEvent
    data object ToggleExpense : ReportEvent
    data object ToggleTransfer : ReportEvent

    data class ToggleCategoryFilter(val categoryId: UUID) : ReportEvent
    data object ToggleUncategorizedFilter : ReportEvent
    data class ToggleAccountFilter(val accountId: UUID) : ReportEvent
    data class ToggleTagFilter(val tagId: UUID) : ReportEvent
    data object ClearFilters : ReportEvent

    data object SelectAllTypes : ReportEvent
    data object SelectAllCategories : ReportEvent
    data object SelectAllAccounts : ReportEvent
    data object SelectAllTags : ReportEvent

    data class AddIncludeKeyword(val keyword: String) : ReportEvent
    data class RemoveIncludeKeyword(val keyword: String) : ReportEvent
    data class AddExcludeKeyword(val keyword: String) : ReportEvent
    data class RemoveExcludeKeyword(val keyword: String) : ReportEvent
    data class SetAmountRange(val min: Float?, val max: Float?) : ReportEvent

    data object ToggleSortOrder : ReportEvent
    data object ToggleAdvanced : ReportEvent
    data object OnExport : ReportEvent
}
