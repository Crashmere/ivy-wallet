package com.ivy.legacy.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.legacy.ui.theme.gradientCutBackgroundBottom

@Composable
fun IvyToolbar(
    onBack: () -> Unit,
    showCloseButton: Boolean = false,
    paddingTop: Dp = 16.dp,
    paddingBottom: Dp = 16.dp,
    Content: @Composable RowScope.() -> Unit = { }
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .gradientCutBackgroundBottom(LegacyTheme.colors.pure, paddingBottom = paddingBottom)
            .padding(top = paddingTop),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(20.dp))

        when (showCloseButton) {
            false -> {
                BackButton(
                    modifier = Modifier.testTag("toolbar_back")
                ) {
                    onBack()
                }
            }

            true -> {
                CloseButton(
                    modifier = Modifier.testTag("toolbar_close")
                ) {
                    onBack()
                }
            }
        }

        Content()
    }
}
