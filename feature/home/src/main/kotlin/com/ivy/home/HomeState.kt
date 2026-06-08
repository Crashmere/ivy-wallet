package com.ivy.home

import androidx.compose.runtime.Immutable
import com.ivy.data.model.Category
import com.ivy.data.model.IncomeExpensePair
import com.ivy.data.model.Theme
import com.ivy.data.model.TransactionHistoryItem
import com.ivy.data.model.legacy.LegacyTransaction
import com.ivy.home.customerjourney.CustomerJourneyCardModel
import com.ivy.legacy.ui.transaction.TransactionListAccount
import com.ivy.ui.period.TimePeriod
import kotlinx.collections.immutable.ImmutableList
import java.math.BigDecimal

@Immutable
internal data class HomeState(
    val theme: Theme,

    val period: TimePeriod,
    val baseData: HomeTransactionListData,

    val history: ImmutableList<TransactionHistoryItem>,
    val stats: IncomeExpensePair,

    val balance: BigDecimal,

    val buffer: BufferInfo,

    val upcoming: HomeDueSection,
    val overdue: HomeDueSection,

    val customerJourneyCards: ImmutableList<CustomerJourneyCardModel>,
    val hideBalance: Boolean,
    val hideIncome: Boolean,
    val expanded: Boolean,
    val shouldShowAccountSpecificColorInTransactions: Boolean
)

@Immutable
internal data class HomeTransactionListData(
    val baseCurrency: String,
    val accounts: ImmutableList<TransactionListAccount>,
    val categories: ImmutableList<Category>
)

@Immutable
internal data class HomeDueSection(
    val transactions: ImmutableList<LegacyTransaction>,
    val expanded: Boolean,
    val stats: IncomeExpensePair
)
