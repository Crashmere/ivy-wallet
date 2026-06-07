package com.ivy.domain.transaction.legacy

import arrow.core.Option
import arrow.core.toOption
import com.ivy.base.model.TransactionType
import com.ivy.domain.time.convertToLocal
import com.ivy.data.model.Expense
import com.ivy.data.model.Income
import com.ivy.data.model.Transaction
import com.ivy.data.model.Transfer
import com.ivy.data.model.legacy.Account
import com.ivy.domain.account.accountCurrency
import java.time.LocalDate
fun expenses(transactions: List<Transaction>): List<Transaction> {
    return transactions.filterIsInstance<Expense>()
}
fun incomes(transactions: List<Transaction>): List<Transaction> {
    return transactions.filterIsInstance<Income>()
}
fun transfers(transactions: List<Transaction>): List<Transaction> {
    return transactions.filterIsInstance<Transfer>()
}
fun isUpcoming(transaction: Transaction, dateNow: LocalDate): Boolean {
    val dueDate = transaction.time.convertToLocal().toLocalDate() ?: return false
    return dateNow.isBefore(dueDate) || dateNow.isEqual(dueDate)
}
fun isOverdue(transaction: Transaction, dateNow: LocalDate): Boolean {
    val dueDate = transaction.time.convertToLocal().toLocalDate() ?: return false
    return dateNow.isAfter(dueDate)
}
fun trnCurrency(
    transaction: Transaction,
    accounts: List<Account>,
    baseCurrency: String
): Option<String> {
    val account = accounts.find {
        it.id == transaction.getAccountId()
    }
        ?: return baseCurrency.toOption()
    return accountCurrency(account, baseCurrency).toOption()
}

object LegacyTrnFunctions {
    fun expenses(transactions: List<com.ivy.base.model.legacy.Transaction>): List<com.ivy.base.model.legacy.Transaction> {
        return transactions.filter { it.type == TransactionType.EXPENSE }
    }
    fun incomes(transactions: List<com.ivy.base.model.legacy.Transaction>): List<com.ivy.base.model.legacy.Transaction> {
        return transactions.filter { it.type == TransactionType.INCOME }
    }
    fun trnCurrency(
        transaction: com.ivy.base.model.legacy.Transaction,
        accounts: List<Account>,
        baseCurrency: String
    ): Option<String> {
        val account = accounts.find { it.id == transaction.accountId }
            ?: return baseCurrency.toOption()
        return accountCurrency(account, baseCurrency).toOption()
    }
}
