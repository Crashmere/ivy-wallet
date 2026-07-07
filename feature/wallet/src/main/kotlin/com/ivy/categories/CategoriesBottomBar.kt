package com.ivy.categories

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.ivy.ui.R
import com.ivy.ui.compose.BackActionBottomBar
import com.ivy.ui.compose.GradientButton
import com.ivy.ui.theme.colors.IvyGradients

@Composable
internal fun BoxWithConstraintsScope.CategoriesBottomBar(
    onClose: () -> Unit,
    onAddCategory: () -> Unit
) {
    BackActionBottomBar(
        pure = CategoriesTheme.colors.pure,
        medium = CategoriesTheme.colors.medium,
        pureInverse = CategoriesTheme.colors.pureInverse,
        onBack = onClose,
    ) {
        GradientButton(
            text = stringResource(R.string.add_category),
            backgroundGradient = IvyGradients.Ivy,
            disabledBackgroundColor = CategoriesTheme.colors.gray,
            shape = CategoriesTheme.shapes.rFull,
            textStyle = CategoriesTheme.typo.b2.copy(
                color = Color(0xFFFAFAFA),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start
            ),
            iconStart = R.drawable.ic_plus,
            iconTint = Color(0xFFFAFAFA),
        ) {
            onAddCategory()
        }
    }
}
