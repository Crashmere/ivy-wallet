package com.ivy.domain.transaction

import arrow.core.Option
import arrow.core.toOption
import com.ivy.data.model.Transaction
import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.data.model.legacy.legacyAccountCurrency

fun transactionCurrency(
    transaction: Transaction,
    accounts: List<LegacyAccount>,
    baseCurrency: String
): Option<String> {
    val account = accounts.find {
        it.id == transaction.getAccountId()
    }
        ?: return baseCurrency.toOption()
    return legacyAccountCurrency(account, baseCurrency).toOption()
}
