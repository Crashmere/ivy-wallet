package com.ivy.loans.model

import com.ivy.data.model.Account
import java.util.UUID

internal data class LoanAccount(
    val id: UUID,
    val name: String,
    val color: Int,
    val icon: String?,
    val currency: String?,
)

internal fun Account.toLoanAccount() = LoanAccount(
    id = id.value,
    name = name.value,
    color = color.value,
    icon = icon?.id,
    currency = asset.code,
)
