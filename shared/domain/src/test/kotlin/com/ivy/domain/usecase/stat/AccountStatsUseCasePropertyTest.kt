package com.ivy.domain.usecase.stat

import arrow.core.NonEmptyList
import com.ivy.data.model.AccountId
import com.ivy.data.model.Expense
import com.ivy.data.model.Income
import com.ivy.data.model.PositiveValue
import com.ivy.data.model.Transaction
import com.ivy.data.model.Transfer
import com.ivy.data.model.getFromAccount
import com.ivy.data.model.getToAccount
import com.ivy.data.model.primitive.AssetCode
import com.ivy.data.model.primitive.NonNegativeInt
import com.ivy.testing.ModelFixtures
import com.ivy.testing.transaction
import com.ivy.domain.model.StatSummary
import com.ivy.domain.model.shouldBeApprox
import com.ivy.domain.nonEmptyExpenses
import com.ivy.domain.nonEmptyIncomes
import com.ivy.domain.nonEmptyTransfersIn
import com.ivy.domain.nonEmptyTransfersOut
import com.ivy.domain.sum
import com.ivy.domain.usecase.account.AccountStats
import com.ivy.domain.usecase.account.AccountStatsUseCase
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AccountStatsUseCasePropertyTest {

    private lateinit var useCase: AccountStatsUseCase

    @Before
    fun setup() {
        useCase = AccountStatsUseCase()
    }

    @Test
    fun `property - ignores irrelevant transactions`() = runTest {
        // given
        val account = ModelFixtures.AccountId
        val arbIrrelevantTransaction = Arb.transaction().filter { transaction ->
            transaction.getFromAccount() != account && transaction.getToAccount() != account
        }

        checkAll(Arb.list(arbIrrelevantTransaction)) { transactions ->
            // when
            val stats = useCase.calculate(account, transactions)

            // then
            stats shouldBe AccountStats.Zero
        }
    }

    @Test
    fun `property - aggregates incomes for account`() = aggregationTestsCase(
        arbTransactions = { acc, asset -> Arb.nonEmptyIncomes(acc, asset) },
        extractValue = Income::value,
        expectedResultSelector = AccountStats::income
    )

    @Test
    fun `property - aggregates expenses for account`() = aggregationTestsCase(
        arbTransactions = { acc, asset -> Arb.nonEmptyExpenses(acc, asset) },
        extractValue = Expense::value,
        expectedResultSelector = AccountStats::expense
    )

    @Test
    fun `property - aggregates transfer-out for account`() = aggregationTestsCase(
        arbTransactions = { acc, asset -> Arb.nonEmptyTransfersOut(acc, asset) },
        extractValue = Transfer::fromValue,
        expectedResultSelector = AccountStats::transfersOut
    )

    @Test
    fun `property - aggregates transfer-in for account`() = aggregationTestsCase(
        arbTransactions = { acc, asset -> Arb.nonEmptyTransfersIn(acc, asset) },
        extractValue = Transfer::toValue,
        expectedResultSelector = AccountStats::transfersIn
    )

    private fun <T : Transaction> aggregationTestsCase(
        arbTransactions: (AccountId, AssetCode) -> Arb<NonEmptyList<T>>,
        extractValue: (T) -> PositiveValue,
        expectedResultSelector: (AccountStats) -> StatSummary,
    ) = runTest {
        // given
        val account = ModelFixtures.AccountId
        val arbEurTransactions = arbTransactions(account, AssetCode.EUR)
        val arbUsdTransactions = arbTransactions(account, AssetCode.USD)
        val arbGpbTransactions = arbTransactions(account, AssetCode.GBP)

        checkAll(
            arbEurTransactions,
            arbUsdTransactions,
            arbGpbTransactions
        ) { eurTransactions, usdTransactions, gbpTransactions ->
            // given
            val transactions = (eurTransactions + usdTransactions + gbpTransactions).shuffled()
            val expectedEur = eurTransactions.map(extractValue).sum()
            val expectedUsd = usdTransactions.map(extractValue).sum()
            val expectedGbp = gbpTransactions.map(extractValue).sum()
            val extractedTransactionsCount = eurTransactions.size + usdTransactions.size + gbpTransactions.size

            // when
            val accStats = useCase.calculate(account, transactions)

            // then
            expectedResultSelector(accStats) shouldBeApprox StatSummary(
                transactionCount = NonNegativeInt.unsafe(extractedTransactionsCount),
                values = mapOf(
                    AssetCode.EUR to expectedEur,
                    AssetCode.USD to expectedUsd,
                    AssetCode.GBP to expectedGbp,
                ),
            )
        }
    }
}
