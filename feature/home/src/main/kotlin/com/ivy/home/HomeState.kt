package com.ivy.home

import androidx.compose.runtime.Immutable
import com.ivy.data.model.Theme
import com.ivy.data.model.TransactionHistoryItem
import com.ivy.home.customerjourney.CustomerJourneyCardModel
import com.ivy.legacy.ui.component.transaction.LegacyDueSection
import com.ivy.legacy.ui.component.transaction.TransactionListData
import com.ivy.ui.period.TimePeriod
import com.ivy.data.model.IncomeExpensePair
import kotlinx.collections.immutable.ImmutableList
import java.math.BigDecimal

@Immutable
data class HomeState(
    val theme: Theme,

    val period: TimePeriod,
    val baseData: TransactionListData,

    val history: ImmutableList<TransactionHistoryItem>,
    val stats: IncomeExpensePair,

    val balance: BigDecimal,

    val buffer: BufferInfo,

    val upcoming: LegacyDueSection,
    val overdue: LegacyDueSection,

    val customerJourneyCards: ImmutableList<CustomerJourneyCardModel>,
    val hideBalance: Boolean,
    val hideIncome: Boolean,
    val expanded: Boolean,
    val shouldShowAccountSpecificColorInTransactions: Boolean
)
