package com.ivy.exchangerates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.ui.navigation.screenScopedViewModel
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.exchangerates.component.RateItem
import com.ivy.exchangerates.model.RateUi
import com.ivy.exchangerates.modal.AddRateModal
import com.ivy.ui.search.SearchInput
import com.ivy.ui.compose.selectEndTextFieldValue
import com.ivy.ui.navigation.navigation
import com.ivy.ui.modal.AmountModal
import java.util.UUID

@Composable
fun BoxWithConstraintsScope.ExchangeRatesScreen() {
    val viewModel: ExchangeRatesViewModel = screenScopedViewModel()
    val state = viewModel.uiState()

    UI(
        state = state,
        onEvent = viewModel::onEvent,
    )
}

@Composable
private fun BoxWithConstraintsScope.UI(
    state: RatesState,
    onEvent: (RatesEvent) -> Unit,
) {
    val nav = navigation()
    var amountModalVisible by remember {
        mutableStateOf(false)
    }
    var rateToUpdate by remember {
        mutableStateOf<RateUi?>(null)
    }
    var amountModalId by remember {
        mutableStateOf(UUID.randomUUID())
    }
    val onRateClick = { rate: RateUi ->
        rateToUpdate = rate
        amountModalId = UUID.randomUUID()
        amountModalVisible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Spacer(Modifier.height(16.dp))
        SearchField(onSearch = { onEvent(RatesEvent.Search(it)) })
        Spacer(Modifier.height(4.dp))
        LazyColumn {
            ratesSection(text = "Manual")
            items(items = state.manual) { rate ->
                Spacer(Modifier.height(4.dp))
                RateItem(
                    rate = rate,
                    onDelete = { onEvent(RatesEvent.RemoveOverride(rate)) },
                    onClick = { onRateClick(rate) }
                )
            }
            ratesSection(text = "Automatic")
            items(items = state.automatic) { rate ->
                Spacer(Modifier.height(4.dp))
                RateItem(
                    rate = rate,
                    onDelete = null,
                    onClick = { onRateClick(rate) }
                )
            }
            item(key = "last_item_spacer") {
                Spacer(Modifier.height(480.dp))
            }
        }
    }

    var addRateModalVisible by remember {
        mutableStateOf(false)
    }

    ExchangeRatesBottomBar(
        onClose = { nav.back() },
        onAddRate = { addRateModalVisible = true }
    )

    AddRateModal(
        visible = addRateModalVisible,
        baseCurrency = state.baseCurrency,
        dismiss = {
            addRateModalVisible = false
        },
        onAdd = { onEvent(RatesEvent.AddRate(it)) }
    )

    AmountModal(
        id = amountModalId,
        visible = amountModalVisible,
        currency = "",
        initialAmount = rateToUpdate?.rate,
        dismiss = {
            amountModalVisible = false
        },
        decimalCountMax = 12,
        Header = {
            rateToUpdate?.let {
                Spacer(Modifier.height(24.dp))
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    text = "${it.from}-${it.to}",
                    style = LegacyTheme.typo.nH2.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = LegacyTheme.colors.primary
                    )
                )
            }
        },
        onAmountChanged = { newRate ->
            rateToUpdate?.let {
                onEvent(RatesEvent.UpdateRate(rateToUpdate!!, newRate))
            }
        }
    )
}

private fun LazyListScope.ratesSection(
    text: String
) {
    item {
        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionDivider()
            Spacer(Modifier.width(16.dp))
            Text(
                text = text,
                style = LegacyTheme.typo.h2
            )
            Spacer(Modifier.width(16.dp))
            SectionDivider()
        }
    }
}

@Composable
private fun RowScope.SectionDivider() {
    Spacer(
        modifier = Modifier
            .weight(1f)
            .height(1.dp)
            .background(LegacyTheme.colors.gray, LegacyTheme.shapes.rFull)
    )
}

@Composable
private fun SearchField(
    onSearch: (String) -> Unit,
) {
    var searchQueryTextFieldValue by remember {
        mutableStateOf(selectEndTextFieldValue(""))
    }

    SearchInput(
        searchQueryTextFieldValue = searchQueryTextFieldValue,
        hint = "Search currency",
        focus = false,
        showClearIcon = searchQueryTextFieldValue.text.isNotEmpty(),
        onSetSearchQueryTextField = {
            searchQueryTextFieldValue = it
            onSearch(it.text)
        }
    )
}
