package com.ivy.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class TransactionType {
    INCOME, EXPENSE, TRANSFER
}
