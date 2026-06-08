package com.ivy.data.api

import com.ivy.data.model.Account
import com.ivy.data.model.AccountId
import com.ivy.data.model.identity.UniqueId
import kotlinx.coroutines.flow.Flow

interface DataChangePublisher {
    val writeEvents: Flow<DataWriteEvent>

    suspend fun post(event: DataWriteEvent)
}

sealed interface DataWriteEvent {
    data object AllDataChange : AccountChange

    sealed interface AccountChange : DataWriteEvent
    data class SaveAccounts(val accounts: List<Account>) : AccountChange
    data class DeleteAccounts(val operation: DeleteOperation<AccountId>) : AccountChange
}

sealed interface DeleteOperation<out Id : UniqueId> {
    data object All : DeleteOperation<Nothing>
    data class Just<Id : UniqueId>(val ids: List<Id>) : DeleteOperation<Id>
}
