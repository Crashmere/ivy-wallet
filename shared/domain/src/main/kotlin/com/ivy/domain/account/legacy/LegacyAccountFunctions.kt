package com.ivy.domain.account.legacy

import com.ivy.data.model.legacy.LegacyAccount

fun includedLegacyAccounts(accounts: List<LegacyAccount>): List<LegacyAccount> =
    accounts.filter { it.includeInBalance }

fun legacyAccountCurrency(account: LegacyAccount, baseCurrency: String): String =
    account.currency ?: baseCurrency
