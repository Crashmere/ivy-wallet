package com.ivy.accounts

internal sealed interface AccountsEvent {
    data class OnReorder(val reorderedList: List<AccountData>) : AccountsEvent
    data class OnReorderModalVisible(val reorderVisible: Boolean) : AccountsEvent
}
