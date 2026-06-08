package com.ivy.accounts

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import com.ivy.data.model.CreateAccountData
import com.ivy.legacy.ui.modal.edit.AccountModal

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
        onCreateAccount = onCreateAccount,
        onEditAccount = { _, _ -> },
        dismiss = dismiss,
    )
}
