package com.ivy.legacy.domain.mapper

import com.ivy.data.db.entity.PlannedPaymentRuleEntity
import com.ivy.legacy.domain.model.PlannedPaymentRule

fun PlannedPaymentRuleEntity.toLegacyDomain(): PlannedPaymentRule = PlannedPaymentRule(
    startDate = startDate,
    intervalN = intervalN,
    intervalType = intervalType,
    oneTime = oneTime,
    type = type,
    accountId = accountId,
    amount = amount,
    categoryId = categoryId,
    title = title,
    description = description,
    isDeleted = isDeleted,
    id = id
)
