package com.ivy.home.customerjourney

import com.ivy.data.model.TransactionType
import com.ivy.base.resource.ResourceProvider
import com.ivy.domain.usecase.home.DismissCustomerJourneyCardUseCase
import com.ivy.domain.usecase.home.GetCustomerJourneyStatsUseCase
import com.ivy.domain.usecase.home.IsCustomerJourneyCardDismissedUseCase
import com.ivy.legacy.ui.theme.system.Gradient
import com.ivy.legacy.ui.theme.system.Ivy
import com.ivy.legacy.ui.theme.system.Orange
import com.ivy.legacy.ui.theme.system.Red
import com.ivy.ui.navigation.EditPlannedScreen
import com.ivy.ui.navigation.MainTab
import com.ivy.ui.navigation.PieChartStatisticScreen
import com.ivy.ui.R
import javax.inject.Inject

class CustomerJourneyCardsProvider @Inject constructor(
    private val getCustomerJourneyStatsUseCase: GetCustomerJourneyStatsUseCase,
    private val isCustomerJourneyCardDismissed: IsCustomerJourneyCardDismissedUseCase,
    private val dismissCustomerJourneyCard: DismissCustomerJourneyCardUseCase,
    private val resourceProvider: ResourceProvider,
) {

    suspend fun loadCards(): List<CustomerJourneyCardModel> {
        val stats = getCustomerJourneyStatsUseCase()

        return activeCards()
            .filter {
                it.condition(
                    stats.transactionCount,
                    stats.plannedPaymentCount
                ) && !isCardDismissed(it)
            }
    }

    private fun isCardDismissed(cardData: CustomerJourneyCardModel): Boolean {
        return isCustomerJourneyCardDismissed(cardData.id)
    }

    fun dismissCard(cardData: CustomerJourneyCardModel) {
        dismissCustomerJourneyCard(cardData.id)
    }

    private fun activeCards() = listOf(
        adjustBalanceCard(),
        addPlannedPaymentCard(),
        expensesPieChartCard()
    )

    private fun adjustBalanceCard() = CustomerJourneyCardModel(
        id = "adjust_balance",
        condition = { trnCount, _ ->
            trnCount == 0L
        },
        title = resourceProvider.getString(R.string.adjust_initial_balance),
        description = resourceProvider.getString(R.string.adjust_initial_balance_description),
        cta = resourceProvider.getString(R.string.to_accounts),
        ctaIcon = R.drawable.ic_custom_account_s,
        background = Gradient.solid(Ivy),
        hasDismiss = false,
        onAction = { _, mainTabState ->
            mainTabState.select(MainTab.ACCOUNTS)
        }
    )

    private fun addPlannedPaymentCard() = CustomerJourneyCardModel(
        id = "add_planned_payment",
        condition = { trnCount, plannedPaymentCount ->
            trnCount >= 1 && plannedPaymentCount == 0L
        },
        title = resourceProvider.getString(R.string.create_first_planned_payment),
        description = resourceProvider.getString(R.string.create_first_planned_payment_description),
        cta = resourceProvider.getString(R.string.add_planned_payment),
        ctaIcon = R.drawable.ic_planned_payments,
        background = Gradient.solid(Orange),
        hasDismiss = true,
        onAction = { navigation, _ ->
            navigation.navigateTo(
                EditPlannedScreen(
                    type = TransactionType.EXPENSE,
                    plannedPaymentRuleId = null
                )
            )
        }
    )

    private fun expensesPieChartCard() = CustomerJourneyCardModel(
        id = "expenses_pie_chart",
        condition = { trnCount, _ ->
            trnCount >= 7
        },
        title = resourceProvider.getString(R.string.did_you_know),
        description = resourceProvider.getString(R.string.you_can_see_a_piechart),
        cta = resourceProvider.getString(R.string.expenses_piechart),
        ctaIcon = R.drawable.ic_custom_bills_s,
        background = Gradient.solid(Red),
        hasDismiss = true,
        onAction = { navigation, _ ->
            navigation.navigateTo(PieChartStatisticScreen(type = TransactionType.EXPENSE))
        }
    )
}
