package com.ivy.accounts

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import com.ivy.data.model.CreateAccountData
import com.ivy.legacy.ui.modal.edit.AccountModal
import com.ivy.legacy.ui.modal.edit.AccountModalSaveData

@Composable
fun BoxWithConstraintsScope.AccountCreationModal(
    visible: Boolean,
    baseCurrency: String,
    onCreateAccount: (CreateAccountData) -> Unit,
    dismiss: () -> Unit,
) {
    AccountModal(
        visible = visible,
        account = null,
        baseCurrency = baseCurrency,
        balance = 0.0,
        onCreateAccount = { onCreateAccount(it.toCreateAccountData()) },
        onEditAccount = { _, _ -> },
        dismiss = dismiss,
    )
}

private fun AccountModalSaveData.toCreateAccountData() = CreateAccountData(
    name = name,
    currency = currency,
    color = color,
    icon = icon,
    balance = balance,
    includeBalance = includeInBalance,
)
