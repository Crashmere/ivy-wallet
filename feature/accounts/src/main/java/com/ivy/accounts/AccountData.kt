package com.ivy.accounts

import com.ivy.data.model.Account
import com.ivy.data.model.Reorderable

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
