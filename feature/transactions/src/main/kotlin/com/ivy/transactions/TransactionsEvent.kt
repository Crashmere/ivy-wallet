package com.ivy.transactions

import com.ivy.data.model.Category
import com.ivy.ui.period.TimePeriod
import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.ui.modal.ChoosePeriodModalData
import java.util.UUID

sealed interface TransactionsEvent {
    data class SetUpcomingExpanded(val expanded: Boolean) : TransactionsEvent
    data class SetOverdueExpanded(val expanded: Boolean) : TransactionsEvent

    data class SetPeriod(
        val period: TimePeriod
    ) : TransactionsEvent

    data object NextMonth : TransactionsEvent
    data object PreviousMonth : TransactionsEvent
    data object Delete : TransactionsEvent
    data class EditCategory(val updatedCategory: Category) : TransactionsEvent
    data class EditAccount(
        val account: LegacyAccount,
        val newBalance: Double
    ) : TransactionsEvent

    data class PayOrGet(
        val transactionId: UUID
    ) : TransactionsEvent

    data class SkipTransaction(
        val transactionId: UUID
    ) : TransactionsEvent

    data class SkipTransactions(
        val transactionIds: List<UUID>
    ) : TransactionsEvent

    data class UpdateAccountDeletionState(val confirmationText: String) : TransactionsEvent
    data class SetSkipAllModalVisible(val visible: Boolean) : TransactionsEvent
    data class OnDeleteModal1Visible(val delete: Boolean) : TransactionsEvent
    data class OnChoosePeriodModalData(val data: ChoosePeriodModalData?) : TransactionsEvent
}
