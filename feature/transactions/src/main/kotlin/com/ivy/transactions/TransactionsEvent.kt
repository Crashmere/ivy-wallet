package com.ivy.transactions

import com.ivy.data.model.Category
import com.ivy.ui.period.TimePeriod
import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.ui.navigation.TransactionsScreen
import com.ivy.ui.modal.ChoosePeriodModalData
import java.util.UUID

sealed interface TransactionsEvent {
    data class SetUpcomingExpanded(val expanded: Boolean) : TransactionsEvent
    data class SetOverdueExpanded(val expanded: Boolean) : TransactionsEvent

    data class SetPeriod(
        val screen: TransactionsScreen,
        val period: TimePeriod
    ) : TransactionsEvent

    data class NextMonth(val screen: TransactionsScreen) : TransactionsEvent
    data class PreviousMonth(val screen: TransactionsScreen) : TransactionsEvent
    data class Delete(val screen: TransactionsScreen) : TransactionsEvent
    data class EditCategory(val updatedCategory: Category) : TransactionsEvent
    data class EditAccount(
        val screen: TransactionsScreen,
        val account: LegacyAccount,
        val newBalance: Double
    ) : TransactionsEvent

    data class PayOrGet(
        val screen: TransactionsScreen,
        val transactionId: UUID
    ) : TransactionsEvent

    data class SkipTransaction(
        val screen: TransactionsScreen,
        val transactionId: UUID
    ) : TransactionsEvent

    data class SkipTransactions(
        val screen: TransactionsScreen,
        val transactionIds: List<UUID>
    ) : TransactionsEvent

    data class UpdateAccountDeletionState(val confirmationText: String) : TransactionsEvent
    data class SetSkipAllModalVisible(val visible: Boolean) : TransactionsEvent
    data class OnDeleteModal1Visible(val delete: Boolean) : TransactionsEvent
    data class OnChoosePeriodModalData(val data: ChoosePeriodModalData?) : TransactionsEvent
}
