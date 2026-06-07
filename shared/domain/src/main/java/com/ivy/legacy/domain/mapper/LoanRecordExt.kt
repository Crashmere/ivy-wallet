package com.ivy.legacy.domain.mapper

import com.ivy.data.db.entity.LoanRecordEntity
import com.ivy.legacy.domain.model.LoanRecord

fun LoanRecordEntity.toLegacyDomain(): LoanRecord = LoanRecord(
    loanId = loanId,
    amount = amount,
    note = note,
    dateTime = dateTime,
    interest = interest,
    accountId = accountId,
    convertedAmount = convertedAmount,
    loanRecordType = loanRecordType,
    isDeleted = isDeleted,
    id = id
)
