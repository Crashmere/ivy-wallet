package com.ivy.accounts

import com.ivy.data.model.Account
import com.ivy.legacy.ui.component.ReorderableItem

internal data class AccountData(
    val account: Account,
    val balance: Double,
    val balanceBaseCurrency: Double?,
    val monthlyExpenses: Double,
    val monthlyIncome: Double
) : ReorderableItem {
    override val orderNum: Double
        get() = account.orderNum

    override fun withNewOrderNum(newOrderNum: Double) = this.copy(
        account = account.copy(
            orderNum = newOrderNum
        )
    )
}
