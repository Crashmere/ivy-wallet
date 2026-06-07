package com.ivy.domain.mapper.legacy

import com.ivy.data.db.entity.LoanEntity
import com.ivy.data.model.LoanType
import com.ivy.data.model.legacy.Loan

fun LoanEntity.toLegacyDomain(): Loan = Loan(
    name = name,
    amount = amount,
    type = type,
    color = color,
    icon = icon,
    orderNum = orderNum,
    accountId = accountId,
    note = note,
    isDeleted = isDeleted,
    id = id,
    dateTime = dateTime
)

fun Loan.toEntity(): LoanEntity = LoanEntity(
    name = name,
    amount = amount,
    type = type,
    color = color,
    icon = icon,
    orderNum = orderNum,
    accountId = accountId,
    note = note,
    isDeleted = isDeleted,
    id = id,
    dateTime = dateTime
)

fun LoanEntity.humanReadableType(): String {
    return if (type == LoanType.BORROW) "BORROWED" else "LENT"
}
