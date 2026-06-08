package com.ivy.accounts

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import com.ivy.data.model.CreateAccountData

@Composable
fun BoxWithConstraintsScope.AccountCreationModal(
    visible: Boolean,
    baseCurrency: String,
    onCreateAccount: (CreateAccountData) -> Unit,
    dismiss: () -> Unit,
) {
    CreateAccountModal(
        visible = visible,
        baseCurrency = baseCurrency,
        balance = 0.0,
        onCreateAccount = onCreateAccount,
        dismiss = dismiss,
    )
}
