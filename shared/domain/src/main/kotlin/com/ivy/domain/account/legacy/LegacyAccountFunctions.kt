package com.ivy.domain.account.legacy

import com.ivy.data.model.legacy.Account

fun includedLegacyAccounts(accounts: List<Account>): List<Account> =
    accounts.filter { it.includeInBalance }

fun legacyAccountCurrency(account: Account, baseCurrency: String): String =
    account.currency ?: baseCurrency
