package com.ivy.accounts

import com.ivy.data.model.AccountId

internal sealed interface AccountsEvent {
    data class OnReorder(val accountIds: List<AccountId>) : AccountsEvent
    data class OnReorderModalVisible(val reorderVisible: Boolean) : AccountsEvent
}
