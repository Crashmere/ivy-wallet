package com.ivy.home

import androidx.compose.runtime.Immutable
import com.ivy.base.theme.Theme
import com.ivy.base.model.legacy.TransactionHistoryItem
import com.ivy.home.customerjourney.CustomerJourneyCardModel
import com.ivy.legacy.ui.model.AppBaseData
import com.ivy.legacy.ui.model.BufferInfo
import com.ivy.legacy.ui.model.LegacyDueSection
import com.ivy.legacy.ui.model.period.TimePeriod
import com.ivy.legacy.domain.pure.data.IncomeExpensePair
import kotlinx.collections.immutable.ImmutableList
import java.math.BigDecimal

@Immutable
data class HomeState(
    val theme: Theme,

    val period: TimePeriod,
    val baseData: AppBaseData,

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
