package com.ivy.legacy.ui.modal

import java.util.UUID

data class AccountModalData(
    val account: AccountModalAccount?,
    val baseCurrency: String,
    val balance: Double,
    val adjustBalanceMode: Boolean = false,
    val forceNonZeroBalance: Boolean = false,
    val autoFocusKeyboard: Boolean = true,
    val id: UUID = UUID.randomUUID()
)

data class AccountModalAccount(
    val id: UUID,
    val name: String,
    val color: Int,
    val currency: String?,
    val icon: String?,
    val includeInBalance: Boolean,
)
