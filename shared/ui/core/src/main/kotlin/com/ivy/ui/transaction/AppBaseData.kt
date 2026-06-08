package com.ivy.ui.transaction

import androidx.compose.runtime.Immutable
import com.ivy.data.model.Category
import com.ivy.data.model.legacy.LegacyAccount
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class AppBaseData(
    val baseCurrency: String,
    val accounts: ImmutableList<LegacyAccount>,
    val categories: ImmutableList<Category>
)
