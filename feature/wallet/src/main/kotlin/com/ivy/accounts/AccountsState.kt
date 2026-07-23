package com.ivy.accounts

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
internal data class AccountsState(
    val baseCurrency: String,
    val accountsData: ImmutableList<AccountData>,
    val netWorth: Double,
    val netWorthChange: Double,
    val reorderVisible: Boolean,
    val compactAccountsModeEnabled: Boolean,
    val hideTotalBalance: Boolean,
)
