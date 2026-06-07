package com.ivy.home.customerjourney

import com.ivy.base.legacy.SharedPrefs
import com.ivy.base.legacy.stringRes
import com.ivy.base.model.TransactionType
import com.ivy.data.db.dao.read.PlannedPaymentRuleDao
import com.ivy.data.repository.TransactionRepository
import com.ivy.legacy.ui.theme.system.Gradient
import com.ivy.legacy.ui.theme.system.Ivy
import com.ivy.legacy.ui.theme.system.Orange
import com.ivy.legacy.ui.theme.system.Red
import com.ivy.navigation.EditPlannedScreen
import com.ivy.navigation.MainTab
import com.ivy.navigation.PieChartStatisticScreen
import com.ivy.ui.R
import javax.inject.Inject

@Deprecated("Legacy code")
class CustomerJourneyCardsProvider @Inject constructor(
  private val transactionRepository: TransactionRepository,
  private val plannedPaymentRuleDao: PlannedPaymentRuleDao,
  private val sharedPrefs: SharedPrefs,
) {

  suspend fun loadCards(): List<CustomerJourneyCardModel> {
    val trnCount = transactionRepository.countHappenedTransactions().value
    val plannedPaymentsCount = plannedPaymentRuleDao.countPlannedPayments()

    return ACTIVE_CARDS
      .filter {
        it.condition(
          trnCount,
          plannedPaymentsCount
        ) && !isCardDismissed(it)
      }
  }

  private fun isCardDismissed(cardData: CustomerJourneyCardModel): Boolean {
    return sharedPrefs.getBoolean(sharedPrefsKey(cardData), false)
  }

  fun dismissCard(cardData: CustomerJourneyCardModel) {
    sharedPrefs.putBoolean(sharedPrefsKey(cardData), true)
  }

  private fun sharedPrefsKey(cardData: CustomerJourneyCardModel): String {
    return "${cardData.id}${SharedPrefs._CARD_DISMISSED}"
  }

  companion object {
    val ACTIVE_CARDS = listOf(
      adjustBalanceCard(),
      addPlannedPaymentCard(),
      didYouKnow_expensesPieChart()
    )

    fun adjustBalanceCard() = CustomerJourneyCardModel(
      id = "adjust_balance",
      condition = { trnCount, _ ->
        trnCount == 0L
      },
      title = stringRes(R.string.adjust_initial_balance),
      description = stringRes(R.string.adjust_initial_balance_description),
      cta = stringRes(R.string.to_accounts),
      ctaIcon = R.drawable.ic_custom_account_s,
      background = Gradient.solid(Ivy),
      hasDismiss = false,
      onAction = { _, mainTabState, _ ->
        mainTabState.select(MainTab.ACCOUNTS)
      }
    )

    fun addPlannedPaymentCard() = CustomerJourneyCardModel(
      id = "add_planned_payment",
      condition = { trnCount, plannedPaymentCount ->
        trnCount >= 1 && plannedPaymentCount == 0L
      },
      title = stringRes(R.string.create_first_planned_payment),
      description = stringRes(R.string.create_first_planned_payment_description),
      cta = stringRes(R.string.add_planned_payment),
      ctaIcon = R.drawable.ic_planned_payments,
      background = Gradient.solid(Orange),
      hasDismiss = true,
      onAction = { navigation, _, _ ->
        navigation.navigateTo(
          EditPlannedScreen(
            type = TransactionType.EXPENSE,
            plannedPaymentRuleId = null
          )
        )
      }
    )

    fun didYouKnow_expensesPieChart() = CustomerJourneyCardModel(
      id = "expenses_pie_chart",
      condition = { trnCount, _ ->
        trnCount >= 7
      },
      title = stringRes(R.string.did_you_know),
      description = stringRes(R.string.you_can_see_a_piechart),
      cta = stringRes(R.string.expenses_piechart),
      ctaIcon = R.drawable.ic_custom_bills_s,
      background = Gradient.solid(Red),
      hasDismiss = true,
      onAction = { navigation, _, _ ->
        navigation.navigateTo(PieChartStatisticScreen(type = TransactionType.EXPENSE))
      }
    )

  }
}
