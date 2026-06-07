package com.ivy.legacy.domain.model

import androidx.compose.runtime.Immutable
import com.ivy.data.model.Account
import com.ivy.data.model.Reorderable

@Immutable
data class AccountData(
    val account: Account,
    val balance: Double,
    val balanceBaseCurrency: Double?,
    val monthlyExpenses: Double,
    val monthlyIncome: Double
) : Reorderable {
    override val orderNum: Double
        get() = account.orderNum

    override fun withNewOrderNum(newOrderNum: Double) = this.copy(
        account = account.copy(
            orderNum = newOrderNum
        )
    )
}
