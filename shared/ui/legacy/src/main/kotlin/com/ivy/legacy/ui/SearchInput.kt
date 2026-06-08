package com.ivy.legacy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.ivy.legacy.ui.theme.system.Gray
import com.ivy.legacy.ui.theme.system.LegacyTheme
import com.ivy.legacy.ui.onScreenStart
import com.ivy.ui.compose.selectEndTextFieldValue
import com.ivy.ui.R
import com.ivy.legacy.ui.component.IvyBasicTextField

@Suppress("MagicNumber")
@Composable
fun SearchInput(
    searchQueryTextFieldValue: TextFieldValue,
    hint: String,
    focus: Boolean = true,
    showClearIcon: Boolean = true,
    onSetSearchQueryTextField: (TextFieldValue) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(LegacyTheme.shapes.rFull)
            .background(LegacyTheme.colors.pure)
            .border(1.dp, Gray, LegacyTheme.shapes.rFull),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SearchIcon(icon = R.drawable.ic_search, modifier = Modifier.weight(1f))

        val searchFocus = FocusRequester()
        IvyBasicTextField(
            modifier = Modifier
                .weight(5f)
                .padding(vertical = 12.dp)
                .focusRequester(searchFocus),
            value = searchQueryTextFieldValue,
            hint = hint,
            onValueChanged = {
                onSetSearchQueryTextField(it)
            }
        )

        if (focus) {
            onScreenStart {
                searchFocus.requestFocus()
            }
        }

        if (showClearIcon) {
            SearchIcon(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onSetSearchQueryTextField(selectEndTextFieldValue(""))
                    },
                icon = R.drawable.ic_outline_clear_24
            )
        }
    }
}

@Composable
private fun SearchIcon(
    modifier: Modifier = Modifier,
    icon: Int,
) {
    Icon(
        modifier = modifier,
        painter = painterResource(id = icon),
        contentDescription = "icon",
        tint = LegacyTheme.colors.pureInverse
    )
}
