package com.ivy.piechart

import androidx.compose.runtime.Immutable
import com.ivy.data.model.legacy.LegacyTransaction
import com.ivy.data.model.TransactionType
import com.ivy.legacy.ui.model.period.TimePeriod
import com.ivy.legacy.ui.modal.ChoosePeriodModalData
import kotlinx.collections.immutable.ImmutableList
import java.util.UUID

@Immutable
data class PieChartStatisticState(
    val transactionType: TransactionType,
    val period: TimePeriod,
    val baseCurrency: String,
    val totalAmount: Double,
    val categoryAmounts: ImmutableList<CategoryAmount>,
    val selectedCategory: SelectedCategory?,
    val accountIdFilterList: ImmutableList<UUID>,
    val showCloseButtonOnly: Boolean,
    val filterExcluded: Boolean,
    val transactions: ImmutableList<LegacyTransaction>,
    val choosePeriodModal: ChoosePeriodModalData?
)
