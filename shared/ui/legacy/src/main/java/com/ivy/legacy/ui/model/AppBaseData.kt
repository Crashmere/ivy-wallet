package com.ivy.legacy.ui.model

import androidx.compose.runtime.Immutable
import com.ivy.data.model.Category
import com.ivy.data.model.legacy.Account
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class AppBaseData(
    val baseCurrency: String,
    val accounts: ImmutableList<Account>,
    val categories: ImmutableList<Category>
)
