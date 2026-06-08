package com.ivy.data.model.legacy

import java.util.UUID

data class LegacyAccount(
    val name: String,
    val color: Int,
    val currency: String? = null,
    val icon: String? = null,
    val orderNum: Double = 0.0,
    val includeInBalance: Boolean = true,

    val isDeleted: Boolean = false,

    val id: UUID = UUID.randomUUID()
)

fun includedLegacyAccounts(accounts: List<LegacyAccount>): List<LegacyAccount> =
    accounts.filter { it.includeInBalance }

fun legacyAccountCurrency(account: LegacyAccount, baseCurrency: String): String =
    account.currency ?: baseCurrency
