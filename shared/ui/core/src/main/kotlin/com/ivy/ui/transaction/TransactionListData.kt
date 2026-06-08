package com.ivy.ui.transaction

import androidx.compose.runtime.Immutable
import java.math.BigDecimal
import java.time.Instant
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
data class TransactionListTag(
    val id: UUID,
    val name: String,
)

@Immutable
data class TransactionListTransaction(
    val id: UUID,
    val accountId: UUID,
    val type: TransactionListTransactionType,
    val amount: BigDecimal,
    val toAccountId: UUID?,
    val toAmount: BigDecimal,
    val title: String?,
    val description: String?,
    val dateTime: Instant?,
    val categoryId: UUID?,
    val dueDate: Instant?,
    val recurringRuleId: UUID?,
    val paidFor: Instant?,
)

enum class TransactionListTransactionType {
    INCOME,
    EXPENSE,
    TRANSFER,
}

@Immutable
data class TransactionListDueSection(
    val transactions: List<TransactionListTransaction>,
    val expanded: Boolean,
    val income: Double,
    val expenses: Double,
)

@Immutable
sealed interface TransactionListHistoryItem

@Immutable
data class TransactionListHistoryTransaction(
    val transaction: TransactionListTransaction,
    val tags: List<TransactionListTag>,
) : TransactionListHistoryItem

@Immutable
data class TransactionListHistoryDateDivider(
    val date: LocalDate,
    val income: Double,
    val expenses: Double,
) : TransactionListHistoryItem
