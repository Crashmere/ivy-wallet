package com.ivy.search

internal sealed interface SearchEvent {
    data class Search(val query: String) : SearchEvent
}
