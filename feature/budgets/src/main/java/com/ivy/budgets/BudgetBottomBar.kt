package com.ivy.budgets

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ivy.ui.R
import com.ivy.legacy.ui.component.BackBottomBar
import com.ivy.legacy.ui.component.IvyButton

@Composable
internal fun BoxWithConstraintsScope.BudgetBottomBar(
    onClose: () -> Unit,
    onAdd: () -> Unit
) {
    BackBottomBar(onBack = onClose) {
        IvyButton(
            text = stringResource(R.string.add_budget),
            iconStart = R.drawable.ic_plus
        ) {
            onAdd()
        }
    }
}
