package com.ivy.base.model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
enum class TransactionType {
    INCOME, EXPENSE, TRANSFER
}
