package com.ivy.domain.account

import com.ivy.data.model.legacy.Account

fun filterExcluded(accounts: List<Account>): List<Account> =
    accounts.filter { it.includeInBalance }

fun accountCurrency(account: Account, baseCurrency: String): String =
    account.currency ?: baseCurrency
