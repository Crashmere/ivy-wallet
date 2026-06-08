package com.ivy.domain.usecase.loan

import com.ivy.data.model.AccountId
import com.ivy.data.model.Expense
import com.ivy.data.model.Income
import com.ivy.data.model.LoanRecordType
import com.ivy.data.model.TransactionType
import com.ivy.data.api.AccountStore
import com.ivy.data.api.CategoryStore
import com.ivy.data.api.LoanRecordStore
import com.ivy.data.api.LoanStore
import com.ivy.data.api.TransactionStore
import com.ivy.data.model.Category
import com.ivy.data.model.CategoryId
import com.ivy.data.model.LoanType
import com.ivy.data.model.PositiveValue
import com.ivy.data.model.Transaction
import com.ivy.data.model.TransactionId
import com.ivy.data.model.TransactionMetadata
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.IconAsset
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.data.model.primitive.PositiveDouble
import com.ivy.domain.usecase.category.GetCategoriesUseCase
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.data.model.Loan
import com.ivy.data.model.LoanRecord
import com.ivy.domain.mapper.legacy.toLegacyAccount
import com.ivy.domain.time.nowUtc
import com.ivy.domain.usecase.exchange.ExchangeAmountUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

internal class LoanTransactionSyncCore @Inject internal constructor(
    private val categoryStore: CategoryStore,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val loanRecordStore: LoanRecordStore,
    private val loanStore: LoanStore,
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val accountStore: AccountStore,
    private val exchangeAmountUseCase: ExchangeAmountUseCase,
    private val transactionRepo: TransactionStore,
) {
    companion object {
        private val DEFAULT_LOAN_CATEGORY_COLOR = 0xFF45E6E6.toInt()
    }

    suspend fun deleteAssociatedTransactions(
        loanId: UUID? = null,
        loanRecordId: UUID? = null
    ) {
        if (loanId == null && loanRecordId == null) {
            return
        }

        withContext(Dispatchers.IO) {
            val transactions: List<Transaction?> =
                if (loanId != null) {
                    transactionRepo.findAllByLoanId(loanId = loanId)
                } else {
                    listOf(transactionRepo.findLoanRecordTransaction(loanRecordId!!))
                }

            transactions.forEach { transaction ->
                deleteTransaction(transaction)
            }
        }
    }

    fun findAccount(
        accounts: List<LegacyAccount>,
        accountId: UUID?,
    ): LegacyAccount? {
        return accountId?.let { uuid ->
            accounts.find { acc ->
                acc.id == uuid
            }
        }
    }

    suspend fun baseCurrency(): String =
        withContext(Dispatchers.IO) { getBaseCurrencyCode() }

    suspend fun updateAssociatedTransaction(
        createTransaction: Boolean,
        loanRecordId: UUID? = null,
        loanId: UUID,
        amount: Double,
        loanType: LoanType,
        selectedAccountId: UUID?,
        title: String? = null,
        category: Category? = null,
        time: Instant? = null,
        isLoanRecord: Boolean = false,
        transaction: Transaction? = null,
        loanRecordType: LoanRecordType
    ) {
        if (isLoanRecord && loanRecordId == null) {
            return
        }

        if (createTransaction && transaction != null) {
            createMainTransaction(
                loanRecordId = loanRecordId,
                loanId = loanId,
                amount = amount,
                loanType = loanType,
                selectedAccountId = selectedAccountId,
                title = title ?: transaction.title?.value,
                categoryId = category?.id?.value ?: transaction.category?.value,
                time = time ?: transaction.time,
                isLoanRecord = isLoanRecord,
                transaction = transaction,
                loanRecordType = loanRecordType
            )
        } else if (createTransaction && transaction == null) {
            createMainTransaction(
                loanRecordId = loanRecordId,
                loanId = loanId,
                amount = amount,
                loanType = loanType,
                selectedAccountId = selectedAccountId,
                title = title,
                categoryId = category?.id?.value,
                time = time ?: nowUtc(),
                isLoanRecord = isLoanRecord,
                transaction = transaction,
                loanRecordType = loanRecordType
            )
        } else {
            deleteTransaction(transaction = transaction)
        }
    }

    private suspend fun createMainTransaction(
        loanRecordId: UUID? = null,
        amount: Double,
        loanType: LoanType,
        loanId: UUID,
        selectedAccountId: UUID?,
        title: String? = null,
        categoryId: UUID? = null,
        time: Instant = nowUtc(),
        isLoanRecord: Boolean = false,
        transaction: Transaction? = null,
        loanRecordType: LoanRecordType
    ) {
        if (selectedAccountId == null) {
            return
        }

        val transactionType = if (isLoanRecord && loanRecordType != LoanRecordType.INCREASE) {
            if (loanType == LoanType.BORROW) TransactionType.EXPENSE else TransactionType.INCOME
        } else if (loanType == LoanType.BORROW) TransactionType.INCOME else TransactionType.EXPENSE

        val transactionCategoryId: UUID? = getCategoryId(existingCategoryId = categoryId)

        withContext(Dispatchers.IO) {
            loanTransaction(
                transactionId = transaction?.id?.value ?: UUID.randomUUID(),
                accountId = selectedAccountId,
                type = transactionType,
                amount = amount,
                categoryId = transactionCategoryId,
                title = title,
                description = transaction?.description?.value,
                time = time,
                recurringRuleId = transaction?.metadata?.recurringRuleId,
                paidFor = transaction?.metadata?.paidForDateTime,
                loanId = loanId,
                loanRecordId = if (isLoanRecord) loanRecordId else null
            )?.let {
                transactionRepo.save(it)
            }
        }
    }

    private suspend fun loanTransaction(
        transactionId: UUID,
        accountId: UUID,
        type: TransactionType,
        amount: Double,
        categoryId: UUID?,
        title: String?,
        description: String?,
        time: Instant,
        recurringRuleId: UUID?,
        paidFor: Instant?,
        loanId: UUID,
        loanRecordId: UUID?,
    ): Transaction? {
        val sourceAccountId = AccountId(accountId)
        val sourceAccount = accountStore.findById(sourceAccountId) ?: return null
        val positiveAmount = PositiveDouble.from(amount).getOrNull() ?: return null
        val value = PositiveValue(
            amount = positiveAmount,
            asset = sourceAccount.asset
        )
        val notBlankTitle = title?.let(NotBlankTrimmedString::from)?.getOrNull()
        val notBlankDescription = description?.let(NotBlankTrimmedString::from)?.getOrNull()
        val category = categoryId?.let(::CategoryId)
        val metadata = TransactionMetadata(
            recurringRuleId = recurringRuleId,
            paidForDateTime = paidFor,
            loanId = loanId,
            loanRecordId = loanRecordId
        )
        val id = TransactionId(transactionId)

        return when (type) {
            TransactionType.INCOME -> Income(
                id = id,
                title = notBlankTitle,
                description = notBlankDescription,
                category = category,
                time = time,
                settled = true,
                metadata = metadata,
                tags = emptyList(),
                value = value,
                account = sourceAccountId,
            )

            TransactionType.EXPENSE -> Expense(
                id = id,
                title = notBlankTitle,
                description = notBlankDescription,
                category = category,
                time = time,
                settled = true,
                metadata = metadata,
                tags = emptyList(),
                value = value,
                account = sourceAccountId,
            )

            TransactionType.TRANSFER -> null
        }
    }

    private suspend fun deleteTransaction(transaction: Transaction?) {
        withContext(Dispatchers.IO) {
            transaction?.let {
                transactionRepo.deleteById(it.id)
            }
        }
    }

    private suspend fun getCategoryId(existingCategoryId: UUID? = null): UUID? {
        if (existingCategoryId != null) {
            return existingCategoryId
        }

        val categoryList = withContext(Dispatchers.IO) { getCategoriesUseCase() }

        var addCategoryToDb = false

        val loanCategory = categoryList.find { category ->
            category.name.value.lowercase(Locale.ENGLISH).contains("loan")
        } ?: run {
            addCategoryToDb = true

            Category(
                name = NotBlankTrimmedString.unsafe("Loans"),
                color = ColorInt(DEFAULT_LOAN_CATEGORY_COLOR),
                icon = IconAsset.unsafe("loan"),
                id = CategoryId(UUID.randomUUID()),
                orderNum = 0.0,
            )
        }

        if (addCategoryToDb) {
            withContext(Dispatchers.IO) {
                loanCategory?.let {
                    categoryStore.save(it)
                }
            }
        }

        return loanCategory?.id?.value
    }

    suspend fun computeConvertedAmount(
        oldLoanRecordAccountId: UUID?,
        oldLoanRecordConvertedAmount: Double?,
        oldLoanRecordAmount: Double,
        newLoanRecordAccountId: UUID?,
        newLoanRecordAmount: Double,
        loanAccountId: UUID?,
        accounts: List<LegacyAccount>,
        reCalculateLoanAmount: Boolean = false,
    ): Double? {
        return withContext(Dispatchers.Default) {
            val newLoanRecordCurrency =
                newLoanRecordAccountId.fetchAssociatedCurrencyCode(accountsList = accounts)

            val oldLoanRecordCurrency =
                oldLoanRecordAccountId.fetchAssociatedCurrencyCode(accountsList = accounts)

            val loanCurrency = loanAccountId.fetchAssociatedCurrencyCode(accountsList = accounts)

            val loanRecordCurrenciesChanged = oldLoanRecordCurrency != newLoanRecordCurrency

            val newConverted: Double? = when {
                newLoanRecordCurrency == loanCurrency -> {
                    null
                }

                reCalculateLoanAmount || loanRecordCurrenciesChanged ||
                        oldLoanRecordConvertedAmount == null -> {
                    withContext(Dispatchers.IO) {
                        convertAmount(
                            baseCurrency = baseCurrency(),
                            amount = newLoanRecordAmount,
                            fromCurrency = newLoanRecordCurrency,
                            toCurrency = loanCurrency
                        )
                    }
                }

                oldLoanRecordAmount != newLoanRecordAmount -> {
                    newLoanRecordAmount * (oldLoanRecordConvertedAmount / oldLoanRecordAmount)
                }

                else -> {
                    oldLoanRecordConvertedAmount
                }
            }
            newConverted
        }
    }

    private suspend fun convertAmount(
        baseCurrency: String,
        amount: Double,
        fromCurrency: String,
        toCurrency: String,
    ): Double {
        return exchangeAmountUseCase(
            amount = amount.toBigDecimal(),
            baseCurrency = baseCurrency,
            fromCurrency = fromCurrency,
            toCurrency = toCurrency
        ).getOrNull()?.toDouble() ?: amount
    }

    private suspend fun UUID?.fetchAssociatedCurrencyCode(accountsList: List<LegacyAccount>): String {
        return findAccount(accountsList, this)?.currency ?: baseCurrency()
    }

    suspend fun fetchAccounts() = withContext(Dispatchers.IO) {
        accountStore.findAll().map { it.toLegacyAccount() }
    }

    suspend fun saveLoanRecords(loanRecords: List<LoanRecord>) = withContext(Dispatchers.IO) {
        loanRecordStore.saveMany(loanRecords)
    }

    suspend fun saveLoanRecords(loanRecord: LoanRecord) = withContext(Dispatchers.IO) {
        loanRecordStore.save(loanRecord)
    }

    suspend fun saveLoan(loan: Loan) = withContext(Dispatchers.IO) {
        loanStore.save(loan)
    }

    suspend fun fetchLoanRecord(loanRecordId: UUID) = withContext(Dispatchers.IO) {
        loanRecordStore.findById(loanRecordId)
    }

    suspend fun fetchAllLoanRecords(loanId: UUID) = withContext(Dispatchers.IO) {
        loanRecordStore.findAllByLoanId(loanId)
    }

    suspend fun fetchLoan(loanId: UUID) = withContext(Dispatchers.IO) {
        loanStore.findById(loanId)
    }

    suspend fun fetchLoanRecordTransaction(loanRecordId: UUID?): Transaction? {
        return loanRecordId?.let {
            withContext(Dispatchers.IO) {
                transactionRepo.findLoanRecordTransaction(it)
            }
        }
    }
}
