package com.ivy.domain.transaction

import arrow.core.Option
import arrow.core.toOption
import com.ivy.data.model.Account
import com.ivy.data.model.Transaction

internal fun transactionCurrency(
    transaction: Transaction,
    accounts: List<Account>,
    baseCurrency: String
): Option<String> {
    val account = accounts.find {
        it.id.value == transaction.getAccountId()
    }
        ?: return baseCurrency.toOption()
    return account.asset.code.toOption()
}
