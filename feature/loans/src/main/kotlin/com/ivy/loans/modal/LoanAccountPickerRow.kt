package com.ivy.loans.modal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.legacy.ui.icon.ItemIconSDefaultIcon
import com.ivy.legacy.ui.icon.IvyIcon
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.ui.theme.colors.findContrastTextColor
import com.ivy.legacy.ui.theme.style
import com.ivy.ui.theme.colors.toComposeColor
import com.ivy.ui.R
import com.ivy.ui.compose.thenIf

@Composable
internal fun LoanAccountPickerRow(
    accounts: List<LegacyAccount>,
    selectedAccount: LegacyAccount?,
    onSelectedAccountChanged: (LegacyAccount) -> Unit,
    onAddNewAccount: () -> Unit,
    modifier: Modifier = Modifier,
    childrenTestTag: String? = null,
) {
    val lazyState = rememberLazyListState()

    LaunchedEffect(accounts, selectedAccount) {
        if (selectedAccount != null) {
            val selectedIndex = accounts.indexOf(selectedAccount)
            if (selectedIndex != -1) {
                lazyState.scrollToItem(selectedIndex)
            }
        }
    }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        state = lazyState
    ) {
        item {
            Spacer(Modifier.width(24.dp))
        }

        itemsIndexed(accounts) { _, account ->
            LoanAccountChip(
                account = account,
                selected = selectedAccount == account,
                testTag = childrenTestTag ?: "account"
            ) {
                onSelectedAccountChanged(account)
            }
        }

        item {
            AddLoanAccountChip {
                onAddNewAccount()
            }
        }

        item {
            Spacer(Modifier.width(24.dp))
        }
    }
}

@Composable
private fun LoanAccountChip(
    account: LegacyAccount,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val accountColor = account.color.toComposeColor()
    val textColor = if (selected) {
        findContrastTextColor(accountColor)
    } else {
        LegacyTheme.colors.pureInverse
    }

    val medium = LegacyTheme.colors.medium
    val rFull = LegacyTheme.shapes.rFull

    Row(
        modifier = Modifier
            .clip(rFull)
            .thenIf(!selected) {
                border(2.dp, medium, rFull)
            }
            .thenIf(selected) {
                background(accountColor, rFull)
            }
            .clickable(onClick = onClick)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(12.dp))

        ItemIconSDefaultIcon(
            iconName = account.icon,
            defaultIcon = R.drawable.ic_custom_account_s,
            tint = textColor
        )

        Spacer(Modifier.width(4.dp))

        Text(
            modifier = Modifier.padding(vertical = 10.dp),
            text = account.name,
            style = LegacyTheme.typo.b2.style(
                color = textColor,
                fontWeight = FontWeight.ExtraBold
            )
        )

        Spacer(Modifier.width(24.dp))
    }

    Spacer(Modifier.width(8.dp))
}

@Composable
private fun AddLoanAccountChip(
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(LegacyTheme.shapes.rFull)
            .border(2.dp, LegacyTheme.colors.medium, LegacyTheme.shapes.rFull)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(12.dp))

        IvyIcon(
            icon = R.drawable.ic_plus,
            tint = LegacyTheme.colors.pureInverse
        )

        Spacer(Modifier.width(4.dp))

        Text(
            modifier = Modifier.padding(vertical = 10.dp),
            text = stringResource(R.string.add_account),
            style = LegacyTheme.typo.b2.style(
                color = LegacyTheme.colors.pureInverse,
                fontWeight = FontWeight.ExtraBold
            )
        )

        Spacer(Modifier.width(24.dp))
    }

    Spacer(Modifier.width(8.dp))
}
