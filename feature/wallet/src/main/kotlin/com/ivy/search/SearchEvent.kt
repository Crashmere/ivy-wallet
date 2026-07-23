package com.ivy.search

import java.util.UUID

internal sealed interface SearchEvent {
    data class Search(val query: String) : SearchEvent
    data class ToggleCategory(val categoryId: UUID) : SearchEvent
    data object ToggleUncategorized : SearchEvent
    data class ToggleAccount(val accountId: UUID) : SearchEvent
    data class ToggleTag(val tagId: UUID) : SearchEvent
    data class SetTimeFilter(val filter: SearchTimeFilter) : SearchEvent
    data object ClearFilters : SearchEvent
}
