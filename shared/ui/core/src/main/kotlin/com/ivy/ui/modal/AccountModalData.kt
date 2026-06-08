package com.ivy.ui.modal

import com.ivy.data.model.legacy.LegacyAccount
import java.util.UUID

data class AccountModalData(
    val account: LegacyAccount?,
    val baseCurrency: String,
    val balance: Double,
    val adjustBalanceMode: Boolean = false,
    val forceNonZeroBalance: Boolean = false,
    val autoFocusKeyboard: Boolean = true,
    val id: UUID = UUID.randomUUID()
)
