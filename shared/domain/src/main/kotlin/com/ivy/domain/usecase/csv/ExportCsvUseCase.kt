package com.ivy.domain.usecase.csv

import com.ivy.data.model.ExternalFile
import com.ivy.data.api.file.TextFileStore
import com.ivy.data.model.Account
import com.ivy.data.model.AccountId
import com.ivy.data.model.Category
import com.ivy.data.model.CategoryId
import com.ivy.data.model.Expense
import com.ivy.data.model.Income
import com.ivy.data.model.Transaction
import com.ivy.data.model.TransactionType
import com.ivy.data.model.Transfer
import com.ivy.data.model.primitive.NonNegativeDouble
import com.ivy.data.model.primitive.toNonNegative
import com.ivy.domain.usecase.account.GetAccountsUseCase
import com.ivy.domain.usecase.category.GetCategoriesUseCase
import com.ivy.domain.usecase.transaction.GetTransactionsUseCase
import com.ivy.domain.time.toLocalDateTimeInSystemZone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.experimental.ExperimentalTypeInference

class ExportCsvUseCase @Inject internal constructor(
    private val getAccountsUseCase: GetAccountsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val textFileStore: TextFileStore,
) {

    suspend fun exportToFile(
        outputFile: ExternalFile,
        exportScope: suspend () -> List<Transaction> = {
            getTransactionsUseCase()
        }
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val csv = exportCsv(exportScope)
        textFileStore.writeText(outputFile, csv)
    }

    suspend fun exportCsv(
        exportScope: suspend () -> List<Transaction>
    ): String = withContext(Dispatchers.IO) {
        val transactions = exportScope()
        val accountsMap = getAccountsUseCase().associateBy(Account::id)
        val categoriesMap = getCategoriesUseCase().associateBy(Category::id)

        buildString {
            append(IvyCsvRow.Columns.joinToString(separator = CSV_SEPARATOR))
            append(NEWLINE)
            for (transaction in transactions) {
                append(
                    transaction.toIvyCsvRow().toCsvString(
                        accountsMap = accountsMap,
                        categoriesMap = categoriesMap
                    )
                )
                append(NEWLINE)
            }
        }
    }

    private fun IvyCsvRow.toCsvString(
        accountsMap: Map<AccountId, Account>,
        categoriesMap: Map<CategoryId, Category>,
    ): String = csvRow {
        // Date
        csvAppend(date?.csvFormat())
        // Title
        csvAppend(title?.value)
        // Category
        csvAppend(categoriesMap[category]?.name?.value)
        // Account
        csvAppend(accountsMap[account]?.name?.value)
        // Amount
        csvAppend(amount.value.csvFormat())
        // Currency
        csvAppend(currency.code)
        // Type
        csvAppend(type.name)
        // Transfer Amount
        csvAppend(transferAmount?.value?.csvFormat())
        // Transfer Currency
        csvAppend(transferCurrency?.code)
        // To Account
        csvAppend(accountsMap[toAccountId]?.name?.value)
        // Receive Amount
        csvAppend(receiveAmount?.value?.csvFormat())
        // Receive Currency
        csvAppend(receiveCurrency?.code)
        // Description
        csvAppend(description?.value)
        // Due Date
        csvAppend(dueData?.csvFormat())
        // ID
        csvAppend(id.value.toString())
    }

    @OptIn(ExperimentalTypeInference::class)
    private fun csvRow(@BuilderInference build: CsvRowScope.() -> Unit): String {
        val columns = mutableListOf<String>()
        val rowScope = object : CsvRowScope {
            override fun csvAppend(value: String?) {
                columns.add(value?.escapeCsvString() ?: "")
            }
        }
        rowScope.build()
        return columns.joinToString(separator = CSV_SEPARATOR)
    }

    private fun String.escapeCsvString(): String {
        val sanitized = escapeSpecialChars()
        return if (sanitized.requiresCsvQuotes()) {
            "\"${sanitized.replace("\"", "\"\"")}\""
        } else {
            sanitized
        }
    }

    private fun String.escapeSpecialChars(): String = replace("\\", "")

    private fun String.requiresCsvQuotes(): Boolean {
        return any { char ->
            char == CSV_SEPARATOR.single() || char == '"' || char == '\n' || char == '\r'
        }
    }

    private fun Transaction.toIvyCsvRow(): IvyCsvRow = when (this) {
        is Expense -> expenseCsvRow()
        is Income -> incomeCsvRow()
        is Transfer -> transferCsvRow()
    }

    private fun Expense.expenseCsvRow(): IvyCsvRow = IvyCsvRow(
        date = time.takeIf
        { settled },
        title = title,
        category = category,
        account = account,
        amount = value.amount.toNonNegative(),
        currency = value.asset,
        type = TransactionType.EXPENSE,
        transferAmount = null,
        transferCurrency = null,
        toAccountId = null,
        receiveAmount = null,
        receiveCurrency = null,
        description = description,
        dueData = time.takeIf
        { !settled },
        id = id
    )

    private fun Income.incomeCsvRow(): IvyCsvRow = IvyCsvRow(
        date = time.takeIf { settled },
        title = title,
        category = category,
        account = account,
        amount = value.amount.toNonNegative(),
        currency = value.asset,
        type = TransactionType.INCOME,
        transferAmount = null,
        transferCurrency = null,
        toAccountId = null,
        receiveAmount = null,
        receiveCurrency = null,
        description = description,
        dueData = time.takeIf { !settled },
        id = id
    )

    private fun Transfer.transferCsvRow(): IvyCsvRow = IvyCsvRow(
        date = time.takeIf { settled },
        title = title,
        category = category,
        account = fromAccount,
        amount = NonNegativeDouble.unsafe(0.0),
        currency = fromValue.asset,
        type = TransactionType.TRANSFER,
        transferAmount = fromValue.amount,
        transferCurrency = fromValue.asset,
        toAccountId = toAccount,
        receiveAmount = toValue.amount,
        receiveCurrency = toValue.asset,
        description = description,
        dueData = time.takeIf { !settled },
        id = id
    )

    private fun Instant.csvFormat(): String =
        toLocalDateTimeInSystemZone().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

    private fun Double.csvFormat(): String = DecimalFormat(NUMBER_FORMAT).apply {
        decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.ENGLISH)
    }.format(this)

    private interface CsvRowScope {
        fun csvAppend(value: String?)
    }

    companion object {
        private const val CSV_SEPARATOR = ","
        private const val NEWLINE = "\n"
        private const val NUMBER_FORMAT = "#,##0.00"
    }
}
