package com.ivy.domain.transaction

import com.ivy.data.model.Expense
import com.ivy.data.model.Income
import com.ivy.data.model.Transaction
import com.ivy.data.model.Transfer
import com.ivy.domain.time.convertToLocal
import java.time.LocalDate

internal fun expenses(transactions: List<Transaction>): List<Transaction> {
    return transactions.filterIsInstance<Expense>()
}

internal fun incomes(transactions: List<Transaction>): List<Transaction> {
    return transactions.filterIsInstance<Income>()
}

internal fun transfers(transactions: List<Transaction>): List<Transaction> {
    return transactions.filterIsInstance<Transfer>()
}

internal fun isUpcoming(transaction: Transaction, dateNow: LocalDate): Boolean {
    val dueDate = transaction.time.convertToLocal().toLocalDate() ?: return false
    return dateNow.isBefore(dueDate) || dateNow.isEqual(dueDate)
}

internal fun isOverdue(transaction: Transaction, dateNow: LocalDate): Boolean {
    val dueDate = transaction.time.convertToLocal().toLocalDate() ?: return false
    return dateNow.isAfter(dueDate)
}
