package com.ivy.piechart

import com.ivy.data.model.TransactionType
import com.ivy.data.model.legacy.LegacyTransaction
import com.ivy.ui.resource.ResourceProvider
import com.ivy.data.model.Category
import com.ivy.data.model.CategoryId
import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.data.model.legacy.includedLegacyAccounts
import com.ivy.data.model.FromToTimeRange
import com.ivy.data.model.IncomeExpenseTransferPair
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.IconAsset
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.domain.usecase.account.GetLegacyAccountsUseCase
import com.ivy.domain.usecase.category.CalculateCategoryIncomeWithAccountFiltersUseCase
import com.ivy.domain.usecase.category.GetCategoriesUseCase
import com.ivy.domain.usecase.transaction.CalculateLegacyTransactionsIncomeExpenseUseCase
import com.ivy.domain.usecase.transaction.GetLegacyTransactionsForAccountsUseCase
import com.ivy.ui.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject

private val AccountTransfersCategoryColorArgb = 0xFFFFCCD5.toInt()

internal class BuildPieChartDataUseCase @Inject internal constructor(
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
            color = ColorInt(AccountTransfersCategoryColorArgb),
            icon = IconAsset.unsafe("transfer"),
            id = CategoryId(UUID.randomUUID()),
            orderNum = 0.0,
        )

    private data class UsableAccounts(
        val accounts: List<LegacyAccount>,
        val accountIdFilterSet: Set<UUID>,
    )

    suspend operator fun invoke(
        baseCurrency: String,
        range: FromToTimeRange,
        type: TransactionType,
        accountIdFilterList: List<UUID>,
        treatTransferAsIncExp: Boolean = false,
        showAccountTransfersCategory: Boolean = treatTransferAsIncExp,
        existingTransactions: List<LegacyTransaction> = emptyList(),
    ): PieChartData {
        val usableAccounts = getUsableAccounts(accountIdFilterList)
        val transactions = existingTransactions.ifEmpty {
            getLegacyTransactionsForAccountsUseCase(
                range = range,
                accountIdFilterSet = usableAccounts.accountIdFilterSet
            )
        }
        val incomeExpenseTransfer = calculateLegacyTransactionsIncomeExpenseUseCase(
            transactions = transactions,
            accounts = usableAccounts.accounts,
            baseCurrency = baseCurrency
        )
        val categoryAmounts = calculateCategoryAmounts(
            type = type,
            baseCurrency = baseCurrency,
            allCategories = getCategoriesUseCase().plus(null),
            transactions = transactions,
            accountsUsed = usableAccounts.accounts,
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
    ): UsableAccounts {
        val allAccounts = getLegacyAccountsUseCase()
        val accountsUsed = if (accountIdFilterList.isEmpty()) {
            includedLegacyAccounts(allAccounts)
        } else {
            allAccounts.filter { accountIdFilterList.contains(it.id) }
        }
        return UsableAccounts(
            accounts = accountsUsed,
            accountIdFilterSet = accountsUsed.map { it.id }.toHashSet()
        )
    }

    private suspend fun calculateCategoryAmounts(
        type: TransactionType,
        baseCurrency: String,
        addAssociatedTransToCategoryAmt: Boolean = false,
        allCategories: List<Category?>,
        transactions: List<LegacyTransaction>,
        accountsUsed: List<LegacyAccount>,
    ): List<CategoryAmount> {
        return allCategories.map { category ->
            val categoryTransactions = if (addAssociatedTransToCategoryAmt) {
                transactions.filter {
                    it.type == type && it.categoryId == category?.id?.value
                }.map { it.toAssociatedTransaction() }
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
        transactions: List<LegacyTransaction>,
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
                }.map { it.toAssociatedTransaction() }

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

private fun LegacyTransaction.toAssociatedTransaction(): AssociatedTransaction {
    return AssociatedTransaction(
        id = id,
        type = type,
    )
}

internal data class PieChartData(
    val totalAmount: Double,
    val categoryAmounts: ImmutableList<CategoryAmount>
)
