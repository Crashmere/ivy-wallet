package com.ivy.categories

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ivy.ui.R
import com.ivy.legacy.ui.component.BackBottomBar
import com.ivy.legacy.ui.component.IvyButton

@Composable
internal fun BoxWithConstraintsScope.CategoriesBottomBar(
    onClose: () -> Unit,
    onAddCategory: () -> Unit
) {
    BackBottomBar(onBack = onClose) {
        IvyButton(
            text = stringResource(R.string.add_category),
            iconStart = R.drawable.ic_plus
        ) {
            onAddCategory()
        }
    }
}
