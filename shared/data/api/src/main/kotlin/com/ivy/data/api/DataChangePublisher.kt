package com.ivy.data.api

import com.ivy.data.model.Account
import com.ivy.data.model.AccountId
import com.ivy.data.model.Category
import com.ivy.data.model.CategoryId
import com.ivy.data.model.Tag
import com.ivy.data.model.TagId
import com.ivy.data.model.identity.UniqueId
import kotlinx.coroutines.flow.Flow

interface DataChangePublisher {
    val writeEvents: Flow<DataWriteEvent>

    suspend fun post(event: DataWriteEvent)
}

sealed interface DataWriteEvent {
    data object AllDataChange : AccountChange, CategoryChange

    sealed interface AccountChange : DataWriteEvent
    data class SaveAccounts(val accounts: List<Account>) : AccountChange
    data class DeleteAccounts(val operation: DeleteOperation<AccountId>) : AccountChange

    sealed interface CategoryChange : DataWriteEvent
    data class SaveCategories(val categories: List<Category>) : CategoryChange
    data class DeleteCategories(val operation: DeleteOperation<CategoryId>) : CategoryChange

    sealed interface TagChange : DataWriteEvent
    data class SaveTags(val tags: List<Tag>) : TagChange
    data class DeleteTags(val operation: DeleteOperation<TagId>) : TagChange
}

sealed interface DeleteOperation<out Id : UniqueId> {
    data object All : DeleteOperation<Nothing>
    data class Just<Id : UniqueId>(val ids: List<Id>) : DeleteOperation<Id>
}
