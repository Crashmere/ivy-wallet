package com.ivy.data.model.legacy

data class CreateBudgetData(
    val name: String,
    val amount: Double,
    val categoryIdsSerialized: String,
    val accountIdsSerialized: String
)
