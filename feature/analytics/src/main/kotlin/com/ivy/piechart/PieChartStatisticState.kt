package com.ivy.piechart

import androidx.compose.runtime.Immutable
import com.ivy.data.model.TransactionType
import com.ivy.ui.period.TimePeriod
import kotlinx.collections.immutable.ImmutableList
import java.util.UUID

internal enum class PieChartGrouping { CATEGORY, ACCOUNT }

@Immutable
internal data class PieChartStatisticState(
    val transactionType: TransactionType,
    val period: TimePeriod,
    val baseCurrency: String,
    val totalAmount: Double,
    val categoryAmounts: ImmutableList<CategoryAmount>,
    val selectedCategory: SelectedCategory?,
    val accountIdFilterList: ImmutableList<UUID>,
    val showCloseButtonOnly: Boolean,
    val filterExcluded: Boolean,
    val grouping: PieChartGrouping,
)
