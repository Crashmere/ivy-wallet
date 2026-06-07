package com.ivy.accounts

sealed interface AccountsEvent {
    data class OnReorder(val reorderedList: List<AccountData>) : AccountsEvent
    data class OnReorderModalVisible(val reorderVisible: Boolean) : AccountsEvent
}
