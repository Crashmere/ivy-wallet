package com.ivy.piechart

import androidx.compose.ui.graphics.toArgb
import com.ivy.base.model.TransactionType
import com.ivy.base.model.legacy.Transaction
import com.ivy.base.resource.ResourceProvider
import com.ivy.data.model.Category
import com.ivy.data.model.CategoryId
import com.ivy.data.model.legacy.Account
import com.ivy.data.model.legacy.FromToTimeRange
import com.ivy.data.model.legacy.IncomeExpenseTransferPair
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.IconAsset
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.domain.usecase.account.GetLegacyAccountsUseCase
import com.ivy.domain.usecase.category.CalculateCategoryIncomeWithAccountFiltersUseCase
import com.ivy.domain.usecase.category.GetCategoriesUseCase
import com.ivy.domain.usecase.transaction.CalculateLegacyTransactionsIncomeExpenseUseCase
import com.ivy.domain.usecase.transaction.GetLegacyTransactionsForAccountsUseCase
import com.ivy.domain.account.filterExcluded
import com.ivy.legacy.ui.theme.system.RedLight
import com.ivy.ui.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject

class BuildPieChartDataUseCase @Inject constructor(
    private val getLegacyAccountsUseCase: GetLegacyAccountsUseCase,
    private val getLegacyTransactionsForAccountsUseCase: GetLegacyTransactionsForAccountsUseCase,
    private val calculateLegacyTransactionsIncomeExpenseUseCase: CalculateLegacyTransactionsIncomeExpenseUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val calculateCategoryIncomeWithAccountFiltersUseCase: CalculateCategoryIncomeWithAccountFiltersUseCase,
    private val resourceProvider: ResourceProvider,
) {
    private val accountTransfersCategory =
        Category(
            name = NotBlankTrimmedString.unsafe(resourceProvider.getString(R.string.account_transfers)),
            color = ColorInt(RedLight.toArgb()),
            icon = IconAsset.unsafe("transfer"),
            id = CategoryId(UUID.randomUUID()),
            orderNum = 0.0,
        )

    suspend operator fun invoke(
        baseCurrency: String,
        range: FromToTimeRange,
        type: TransactionType,
        accountIdFilterList: List<UUID>,
        treatTransferAsIncExp: Boolean = false,
        showAccountTransfersCategory: Boolean = treatTransferAsIncExp,
        existingTransactions: List<Transaction> = emptyList(),
    ): PieChartData {
        val (accountsUsed, accountIdFilterSet) = getUsableAccounts(accountIdFilterList)
        val transactions = existingTransactions.ifEmpty {
            getLegacyTransactionsForAccountsUseCase(
                range = range,
                accountIdFilterSet = accountIdFilterSet
            )
        }
        val incomeExpenseTransfer = calculateLegacyTransactionsIncomeExpenseUseCase(
            transactions = transactions,
            accounts = accountsUsed,
            baseCurrency = baseCurrency
        )
        val categoryAmounts = calculateCategoryAmounts(
            type = type,
            baseCurrency = baseCurrency,
            allCategories = getCategoriesUseCase().plus(null),
            transactions = transactions,
            accountsUsed = accountsUsed,
            addAssociatedTransToCategoryAmt = existingTransactions.isNotEmpty()
        )
        val categoryAmountsWithTransfers = addAccountTransfersCategory(
            showAccountTransfersCategory = showAccountTransfersCategory,
            type = type,
            accountTransfersCategory = accountTransfersCategory,
            accountIdFilterSet = accountIdFilterList.toHashSet(),
            incomeExpenseTransfer = incomeExpenseTransfer,
            categoryAmounts = categoryAmounts,
            transactions = transactions
        )
        val totalAmount = calculateTotalAmount(
            type = type,
            treatTransferAsIncExp = treatTransferAsIncExp,
            incomeExpenseTransfer = incomeExpenseTransfer
        )

        return PieChartData(
            totalAmount = totalAmount.toDouble(),
            categoryAmounts = categoryAmountsWithTransfers.toImmutableList()
        )
    }

    private suspend fun getUsableAccounts(
        accountIdFilterList: List<UUID>,
    ): Pair<List<Account>, Set<UUID>> {
        val allAccounts = getLegacyAccountsUseCase()
        val accountsUsed = if (accountIdFilterList.isEmpty()) {
            filterExcluded(allAccounts)
        } else {
            allAccounts.filter { accountIdFilterList.contains(it.id) }
        }
        return accountsUsed to accountsUsed.map { it.id }.toHashSet()
    }

    private suspend fun calculateCategoryAmounts(
        type: TransactionType,
        baseCurrency: String,
        addAssociatedTransToCategoryAmt: Boolean = false,
        allCategories: List<Category?>,
        transactions: List<Transaction>,
        accountsUsed: List<Account>,
    ): List<CategoryAmount> {
        return allCategories.map { category ->
            val categoryTransactions = if (addAssociatedTransToCategoryAmt) {
                transactions.filter {
                    it.type == type && it.categoryId == category?.id?.value
                }
            } else {
                emptyList()
            }
            val catIncomeExpense = calculateCategoryIncomeWithAccountFiltersUseCase(
                transactions = transactions,
                accountFilterList = accountsUsed,
                category = category,
                baseCurrency = baseCurrency
            )

            CategoryAmount(
                category = category,
                amount = when (type) {
                    TransactionType.INCOME -> catIncomeExpense.income.toDouble()
                    TransactionType.EXPENSE -> catIncomeExpense.expense.toDouble()
                    else -> error("not supported transactionType - $type")
                },
                associatedTransactions = categoryTransactions,
                isCategoryUnspecified = category == null
            )
        }.filter {
            it.amount != 0.0
        }.sortedByDescending {
            it.amount
        }
    }

    private fun calculateTotalAmount(
        type: TransactionType,
        treatTransferAsIncExp: Boolean,
        incomeExpenseTransfer: IncomeExpenseTransferPair,
    ): BigDecimal {
        return when (type) {
            TransactionType.INCOME -> {
                incomeExpenseTransfer.income +
                        if (treatTransferAsIncExp) {
                            incomeExpenseTransfer.transferIncome
                        } else {
                            BigDecimal.ZERO
                        }
            }

            TransactionType.EXPENSE -> {
                incomeExpenseTransfer.expense +
                        if (treatTransferAsIncExp) {
                            incomeExpenseTransfer.transferExpense
                        } else {
                            BigDecimal.ZERO
                        }
            }

            else -> BigDecimal.ZERO
        }
    }

    private fun addAccountTransfersCategory(
        showAccountTransfersCategory: Boolean,
        type: TransactionType,
        accountTransfersCategory: Category,
        accountIdFilterSet: Set<UUID>,
        transactions: List<Transaction>,
        incomeExpenseTransfer: IncomeExpenseTransferPair,
        categoryAmounts: List<CategoryAmount>,
    ): List<CategoryAmount> {
        return if (
            !showAccountTransfersCategory ||
            incomeExpenseTransfer.transferIncome == BigDecimal.ZERO &&
            incomeExpenseTransfer.transferExpense == BigDecimal.ZERO
        ) {
            categoryAmounts.sortedByDescending { it.amount }
        } else {
            val amount = if (type == TransactionType.INCOME) {
                incomeExpenseTransfer.transferIncome.toDouble()
            } else {
                incomeExpenseTransfer.transferExpense.toDouble()
            }
            val categoryTransactions = transactions
                .filter { it.type == TransactionType.TRANSFER }
                .filter {
                    if (type == TransactionType.EXPENSE) {
                        accountIdFilterSet.contains(it.accountId)
                    } else {
                        accountIdFilterSet.contains(it.toAccountId)
                    }
                }

            categoryAmounts.plus(
                CategoryAmount(
                    category = accountTransfersCategory,
                    amount = amount,
                    associatedTransactions = categoryTransactions,
                    isCategoryUnspecified = true
                )
            ).sortedByDescending {
                it.amount
            }
        }
    }
}

data class PieChartData(
    val totalAmount: Double,
    val categoryAmounts: ImmutableList<CategoryAmount>
)
