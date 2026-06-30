package com.ivy.bulkedit

import com.ivy.ui.period.TimePeriod
import java.util.UUID

internal sealed interface BulkEditEvent {
    // Time range
    data object OnPreviousMonth : BulkEditEvent
    data object OnNextMonth : BulkEditEvent
    data class OnSelectPeriod(val period: TimePeriod) : BulkEditEvent

    // Filtering
    data class ToggleCategoryFilter(val categoryId: UUID) : BulkEditEvent
    data object ToggleUncategorizedFilter : BulkEditEvent
    data class ToggleTagFilter(val tagId: UUID) : BulkEditEvent
    data object ClearFilters : BulkEditEvent

    // Bulk changes applied to the currently matched transactions
    data class ApplyCategoryChange(val categoryId: UUID?) : BulkEditEvent
    data class ApplyAccountChange(val accountId: UUID) : BulkEditEvent
    data class ApplyAddTag(val tagId: UUID) : BulkEditEvent
    data class ApplyRemoveTag(val tagId: UUID) : BulkEditEvent
}
