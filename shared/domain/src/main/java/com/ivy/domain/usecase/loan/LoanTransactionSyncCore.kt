package com.ivy.domain.usecase.loan

import com.ivy.base.model.legacy.Transaction
import com.ivy.base.model.LoanRecordType
import com.ivy.base.model.TransactionType
import com.ivy.base.time.TimeProvider
import com.ivy.data.api.AccountStore
import com.ivy.data.api.CategoryStore
import com.ivy.data.api.LoanRecordStore
import com.ivy.data.api.LoanStore
import com.ivy.data.api.TransactionStore
import com.ivy.data.model.Category
import com.ivy.data.model.CategoryId
import com.ivy.data.model.LoanType
import com.ivy.data.model.TransactionId
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.IconAsset
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.domain.usecase.category.GetCategoriesUseCase
import com.ivy.domain.usecase.currency.GetBaseCurrencyCodeUseCase
import com.ivy.data.model.legacy.Account
import com.ivy.data.model.legacy.Loan
import com.ivy.data.model.legacy.LoanRecord
import com.ivy.domain.mapper.legacy.toDomain
import com.ivy.domain.mapper.legacy.toLegacy
import com.ivy.domain.mapper.legacy.toLegacyDomain
import com.ivy.base.coroutines.computationThread
import com.ivy.base.coroutines.ioThread
import com.ivy.domain.usecase.exchange.LegacyExchangeRatesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class LoanTransactionSyncCore @Inject constructor(
    private val categoryStore: CategoryStore,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val loanRecordStore: LoanRecordStore,
    private val loanStore: LoanStore,
    private val getBaseCurrencyCode: GetBaseCurrencyCodeUseCase,
    private val accountStore: AccountStore,
    private val exchangeRatesLogic: LegacyExchangeRatesUseCase,
    private val transactionRepo: TransactionStore,
    private val timeProvider: TimeProvider,
) {
    private var baseCurrencyCode: String? = null

    companion object {
        val DEFAULT_LOAN_CATEGORY_COLOR = 0xFF45E6E6.toInt()
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            baseCurrencyCode = baseCurrency()
        }
    }

    suspend fun deleteAssociatedTransactions(
        loanId: UUID? = null,
        loanRecordId: UUID? = null
    ) {
        if (loanId == null && loanRecordId == null) {
            return
        }

        ioThread {
            val transactions: List<Transaction?> =
                if (loanId != null) {
                    transactionRepo.findAllByLoanId(loanId = loanId)
                        .map { it.toLegacy() }
                } else {
                    listOf(transactionRepo.findLoanRecordTransaction(loanRecordId!!)?.toLegacy())
                }

            transactions.forEach { trans ->
                deleteTransaction(trans)
            }
        }
    }

    fun findAccount(
        accounts: List<Account>,
        accountId: UUID?,
    ): Account? {
        return accountId?.let { uuid ->
            accounts.find { acc ->
                acc.id == uuid
            }
        }
    }

    suspend fun baseCurrency(): String =
        ioThread { baseCurrencyCode ?: getBaseCurrencyCode() }

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
                title = title ?: transaction.title,
                categoryId = category?.id?.value ?: transaction.categoryId,
                time = time ?: transaction.dateTime ?: timeProvider.utcNow(),
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
                time = time ?: timeProvider.utcNow(),
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
        time: Instant = timeProvider.utcNow(),
        isLoanRecord: Boolean = false,
        transaction: Transaction? = null,
        loanRecordType: LoanRecordType
    ) {
        if (selectedAccountId == null) {
            return
        }

        val transType = if (isLoanRecord && loanRecordType != LoanRecordType.INCREASE) {
            if (loanType == LoanType.BORROW) TransactionType.EXPENSE else TransactionType.INCOME
        } else if (loanType == LoanType.BORROW) TransactionType.INCOME else TransactionType.EXPENSE

        val transCategoryId: UUID? = getCategoryId(existingCategoryId = categoryId)

        val modifiedTransaction: Transaction = transaction?.copy(
            loanId = loanId,
            loanRecordId = if (isLoanRecord) loanRecordId else null,
            amount = amount.toBigDecimal(),
            type = transType,
            accountId = selectedAccountId,
            title = title,
            categoryId = transCategoryId,
            dateTime = time
        )
            ?: Transaction(
                accountId = selectedAccountId,
                type = transType,
                amount = amount.toBigDecimal(),
                dateTime = time,
                categoryId = transCategoryId,
                title = title,
                loanId = loanId,
                loanRecordId = if (isLoanRecord) loanRecordId else null
            )

        ioThread {
            modifiedTransaction.toDomain(accountStore)?.let {
                transactionRepo.save(it)
            }
        }
    }

    private suspend fun deleteTransaction(transaction: Transaction?) {
        ioThread {
            transaction?.let {
                transactionRepo.deleteById(TransactionId(it.id))
            }
        }
    }

    private suspend fun getCategoryId(existingCategoryId: UUID? = null): UUID? {
        if (existingCategoryId != null) {
            return existingCategoryId
        }

        val categoryList = ioThread { getCategoriesUseCase() }

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
            ioThread {
                loanCategory?.let {
                    categoryStore.save(it)
                }
            }
        }

        return loanCategory?.id?.value
    }

    suspend fun computeConvertedAmount(
        oldLoanRecordAccountId: UUID?,
        oldLonRecordConvertedAmount: Double?,
        oldLoanRecordAmount: Double,
        newLoanRecordAccountID: UUID?,
        newLoanRecordAmount: Double,
        loanAccountId: UUID?,
        accounts: List<Account>,
        reCalculateLoanAmount: Boolean = false,
    ): Double? {
        return computationThread {
            val newLoanRecordCurrency =
                newLoanRecordAccountID.fetchAssociatedCurrencyCode(accountsList = accounts)

            val oldLoanRecordCurrency =
                oldLoanRecordAccountId.fetchAssociatedCurrencyCode(accountsList = accounts)

            val loanCurrency = loanAccountId.fetchAssociatedCurrencyCode(accountsList = accounts)

            val loanRecordCurrenciesChanged = oldLoanRecordCurrency != newLoanRecordCurrency

            val newConverted: Double? = when {
                newLoanRecordCurrency == loanCurrency -> {
                    null
                }

                reCalculateLoanAmount || loanRecordCurrenciesChanged ||
                        oldLonRecordConvertedAmount == null -> {
                    ioThread {
                        exchangeRatesLogic.convertAmount(
                            baseCurrency = baseCurrency(),
                            amount = newLoanRecordAmount,
                            fromCurrency = newLoanRecordCurrency,
                            toCurrency = loanCurrency
                        )
                    }
                }

                oldLoanRecordAmount != newLoanRecordAmount -> {
                    newLoanRecordAmount * (oldLonRecordConvertedAmount / oldLoanRecordAmount)
                }

                else -> {
                    oldLonRecordConvertedAmount
                }
            }
            newConverted
        }
    }

    private suspend fun UUID?.fetchAssociatedCurrencyCode(accountsList: List<Account>): String {
        return findAccount(accountsList, this)?.currency ?: baseCurrency()
    }

    suspend fun fetchAccounts() = ioThread {
        accountStore.findAll().map { it.toLegacyDomain() }
    }

    suspend fun saveLoanRecords(loanRecords: List<LoanRecord>) = ioThread {
        loanRecordStore.saveMany(loanRecords)
    }

    suspend fun saveLoanRecords(loanRecord: LoanRecord) = ioThread {
        loanRecordStore.save(loanRecord)
    }

    suspend fun saveLoan(loan: Loan) = ioThread {
        loanStore.save(loan)
    }

    suspend fun fetchLoanRecord(loanRecordId: UUID) = ioThread {
        loanRecordStore.findById(loanRecordId)
    }

    suspend fun fetchAllLoanRecords(loanId: UUID) = ioThread {
        loanRecordStore.findAllByLoanId(loanId)
    }

    suspend fun fetchLoan(loanId: UUID) = ioThread {
        loanStore.findById(loanId)
    }

    suspend fun fetchLoanRecordTransaction(loanRecordId: UUID?): Transaction? {
        return loanRecordId?.let {
            ioThread {
                transactionRepo.findLoanRecordTransaction(it)?.toLegacy()
            }
        }
    }
}
