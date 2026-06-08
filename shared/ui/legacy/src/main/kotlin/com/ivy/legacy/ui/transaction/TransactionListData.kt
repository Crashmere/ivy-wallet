package com.ivy.legacy.ui.transaction

import androidx.compose.runtime.Immutable
import com.ivy.data.model.Tag
import com.ivy.data.model.Transaction
import java.time.LocalDate
import java.util.UUID

@Immutable
data class TransactionListData(
    val baseCurrency: String,
    val accounts: List<TransactionListAccount>,
    val categories: List<TransactionListCategory>
)

@Immutable
data class TransactionListAccount(
    val id: UUID,
    val name: String,
    val color: Int,
    val icon: String?,
    val currency: String?,
)

@Immutable
data class TransactionListCategory(
    val id: UUID,
    val name: String,
    val color: Int,
    val icon: String?,
)

@Immutable
data class TransactionListDueSection(
    val transactions: List<Transaction>,
    val expanded: Boolean,
    val income: Double,
    val expenses: Double,
)

@Immutable
sealed interface TransactionListHistoryItem

@Immutable
data class TransactionListHistoryTransaction(
    val transaction: Transaction,
    val tags: List<Tag>,
) : TransactionListHistoryItem

@Immutable
data class TransactionListHistoryDateDivider(
    val date: LocalDate,
    val income: Double,
    val expenses: Double,
) : TransactionListHistoryItem
