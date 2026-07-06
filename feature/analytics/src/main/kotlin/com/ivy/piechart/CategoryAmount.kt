package com.ivy.piechart

import androidx.compose.runtime.Immutable
import com.ivy.data.model.Account
import com.ivy.data.model.Category
import com.ivy.data.model.TransactionType
import java.util.UUID

@Immutable
internal data class CategoryAmount(
    val category: Category?,
    val amount: Double,
    val associatedTransactions: List<AssociatedTransaction> = emptyList(),
    val isCategoryUnspecified: Boolean = false,
    // When the pie chart is grouped by account, this holds the account backing the
    // slice; [category] is then a synthetic entry carrying the account's name/color/icon.
    val account: Account? = null,
)

@Immutable
internal data class AssociatedTransaction(
    val id: UUID,
    val type: TransactionType,
)
