package com.ivy.legacy.ui.component.transaction

import androidx.compose.runtime.Immutable
import com.ivy.data.model.Category
import com.ivy.data.model.legacy.LegacyAccount
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class TransactionListData(
    val baseCurrency: String,
    val accounts: ImmutableList<LegacyAccount>,
    val categories: ImmutableList<Category>
)
