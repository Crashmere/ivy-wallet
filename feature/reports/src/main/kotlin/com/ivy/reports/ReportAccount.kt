package com.ivy.reports

import androidx.compose.runtime.Immutable
import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.legacy.ui.transaction.TransactionListAccount
import java.util.UUID

@Immutable
internal data class ReportAccount(
    val id: UUID,
    val name: String,
    val color: Int,
    val icon: String?,
    val currency: String?,
    val orderNum: Double,
)

internal fun LegacyAccount.toReportAccount() = ReportAccount(
    id = id,
    name = name,
    color = color,
    icon = icon,
    currency = currency,
    orderNum = orderNum,
)

internal fun ReportAccount.toTransactionListAccount() = TransactionListAccount(
    id = id,
    name = name,
    color = color,
    icon = icon,
    currency = currency,
)
