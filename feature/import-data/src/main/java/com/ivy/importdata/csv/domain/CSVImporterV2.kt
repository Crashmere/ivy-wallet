package com.ivy.importdata.csv.domain

import androidx.compose.ui.graphics.toArgb
import arrow.core.raise.either
import com.ivy.base.model.legacy.Transaction
import com.ivy.base.model.TransactionType
import com.ivy.base.time.TimeConverter
import com.ivy.data.model.Category
import com.ivy.data.model.CategoryId
import com.ivy.data.model.importing.ImportCsvRow
import com.ivy.data.model.importing.ImportResult
import com.ivy.data.model.primitive.AssetCode
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.IconAsset
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.domain.usecase.account.SaveAccountUseCase
import com.ivy.domain.usecase.category.GetCategoriesUseCase
import com.ivy.domain.usecase.category.SaveCategoryUseCase
import com.ivy.domain.usecase.currency.GetBaseCurrencyUseCase
import com.ivy.domain.usecase.transaction.SaveLegacyTransactionUseCase
import com.ivy.legacy.ui.theme.IVY_COLOR_PICKER_COLORS_FREE
import com.ivy.importdata.csv.ImportantFields
import com.ivy.importdata.csv.OptionalFields
import com.ivy.importdata.csv.TransferFields
import com.ivy.data.model.legacy.Account
import com.ivy.base.text.toLowerCaseLocal
import com.ivy.data.model.currency.IvyCurrency
import com.ivy.domain.usecase.account.GetLegacyAccountsUseCase
import com.ivy.legacy.domain.pure.util.nextOrderNum
import com.ivy.legacy.ui.theme.Green
import com.ivy.legacy.ui.theme.IvyDark
import kotlinx.collections.immutable.toImmutableList
import java.util.UUID
import javax.inject.Inject
import kotlin.math.absoluteValue
import com.ivy.importdata.csv.CSVRow as CSVRowNew

class CSVImporterV2 @Inject constructor(
    private val getLegacyAccountsUseCase: GetLegacyAccountsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getBaseCurrency: GetBaseCurrencyUseCase,
    private val saveAccountUseCase: SaveAccountUseCase,
    private val saveCategoryUseCase: SaveCategoryUseCase,
    private val saveLegacyTransactionUseCase: SaveLegacyTransactionUseCase,
    private val timeConverter: TimeConverter,
) {

    lateinit var accounts: List<Account>
    lateinit var categories: List<Category>

    private var newCategoryColorIndex = 0
    private var newAccountColorIndex = 0

    suspend fun import(
        csv: List<CSVRowNew>,
        importantFields: ImportantFields,
        transferFields: TransferFields,
        optionalFields: OptionalFields,
        onProgress: suspend (progressPercent: Double) -> Unit,
    ): ImportResult {
        val rows = csv.drop(1) // drop the header
        val rowsCount = rows.size

        newCategoryColorIndex = 0
        newAccountColorIndex = 0

        accounts = getLegacyAccountsUseCase()
        val initialAccountsCount = accounts.size

        categories = getCategoriesUseCase()
        val initialCategoriesCount = categories.size

        val baseCurrency = getBaseCurrency()

        val failedRows = mutableListOf<ImportCsvRow>()

        val transactions = rows.mapIndexedNotNull { index, row ->
            val progressPercent = if (rowsCount > 0) {
                index / rowsCount.toDouble()
            } else {
                0.0
            }
            onProgress(progressPercent / 2)

            val transaction = mapToTransaction(
                baseCurrency = baseCurrency,
                importantFields = importantFields,
                transferFields = transferFields,
                optionalFields = optionalFields,
                row = row,
            )

            if (transaction == null) {
                failedRows.add(
                    ImportCsvRow(
                        index = index + 2, // + 1 because we skip Header and +1 because they don't start from zero
                        content = row.values
                    )
                )
            }
            transaction
        }

        for ((index, transaction) in transactions.withIndex()) {
            val progressPercent = if (rowsCount > 0) {
                index / transactions.size.toDouble()
            } else {
                0.0
            }
            onProgress(0.5 + progressPercent / 2)
            saveLegacyTransactionUseCase(transaction)
        }

        return ImportResult(
            rowsFound = rowsCount,
            transactionsImported = transactions.size,
            accountsImported = accounts.size - initialAccountsCount,
            categoriesImported = categories.size - initialCategoriesCount,
            failedRows = failedRows.toImmutableList()
        )
    }

    private suspend fun mapToTransaction(
        baseCurrency: AssetCode,
        row: CSVRowNew,
        importantFields: ImportantFields,
        transferFields: TransferFields,
        optionalFields: OptionalFields,
    ): Transaction? {
        val type = parseTransactionType(
            value = row.extractValue(importantFields.type),
            metadata = importantFields.type.metadata,
        ) ?: return null

        val toAccount = if (type == TransactionType.TRANSFER) {
            mapAccount(
                baseCurrency = baseCurrency,
                accountNameString = parseToAccount(
                    value = row.extractValue(transferFields.toAccount),
                    metadata = transferFields.toAccount.metadata
                ),
                currencyRawString = parseToAccountCurrency(
                    value = row.extractValue(transferFields.toAccountCurrency),
                    metadata = transferFields.toAccountCurrency.metadata
                ) ?: parseAccountCurrency(
                    value = row.extractValue(importantFields.accountCurrency),
                    metadata = importantFields.accountCurrency.metadata,
                ),
                color = null,
                icon = null,
                orderNum = null,
            )
        } else {
            null
        }

        val csvAmount = if (type != TransactionType.TRANSFER) {
            parseAmount(
                value = row.extractValue(importantFields.amount),
                metadata = importantFields.amount.metadata
            )
        } else {
            parseAmount(
                value = row.extractValue(transferFields.toAmount),
                metadata = transferFields.toAmount.metadata
            )
        } ?: return null
        val amount = csvAmount.absoluteValue

        if (amount <= 0) {
            // Cannot save transactions with zero amount
            return null
        }

        val toAmount = if (type == TransactionType.TRANSFER) {
            parseAmount(
                value = row.extractValue(transferFields.toAmount),
                metadata = transferFields.toAmount.metadata
            )
        } else {
            null
        }

        val dateTime = parseDate(
            row.extractValue(importantFields.date),
            importantFields.date.metadata
        ) ?: return null

        val account = mapAccount(
            baseCurrency = baseCurrency,
            accountNameString = parseAccount(
                value = row.extractValue(importantFields.account),
                metadata = importantFields.account.metadata
            ),
            currencyRawString = parseAccountCurrency(
                value = row.extractValue(importantFields.accountCurrency),
                metadata = importantFields.accountCurrency.metadata
            ),
            color = null,
            icon = null,
            orderNum = null,
        ) ?: return null

        val category = mapCategory(
            categoryNameString = parseCategory(
                value = row.extractValue(optionalFields.category),
                metadata = optionalFields.category.metadata
            ),
            color = null,
            icon = null,
            orderNum = null,
        )
        val title = parseTitle(
            row.extractValue(optionalFields.title),
            optionalFields.title.metadata
        )
        val description = parseTitle(
            row.extractValue(optionalFields.description),
            optionalFields.description.metadata
        )

        return Transaction(
            id = UUID.randomUUID(),
            type = type,
            amount = amount.toBigDecimal(),
            accountId = account.id,
            toAccountId = toAccount?.id,
            toAmount = toAmount?.toBigDecimal() ?: amount.toBigDecimal(),
            dateTime = with(timeConverter) { dateTime.toUTC() },
            dueDate = null,
            categoryId = category?.id?.value,
            title = title,
            description = description
        )
    }

    private suspend fun mapAccount(
        baseCurrency: AssetCode,
        accountNameString: String?,
        color: Int?,
        icon: String?,
        orderNum: Double?,
        currencyRawString: String?,
    ): Account? {
        if (accountNameString == null || accountNameString.isBlank()) return null

        val existingAccount = accounts.firstOrNull {
            accountNameString.toLowerCaseLocal() == it.name.toLowerCaseLocal()
        }
        if (existingAccount != null) {
            return existingAccount
        }

        // create new account
        val colorArgb = color ?: when {
            accountNameString.toLowerCaseLocal().contains("cash") -> {
                Green
            }

            accountNameString.toLowerCaseLocal().contains("revolut") -> {
                IvyDark
            }

            else -> IVY_COLOR_PICKER_COLORS_FREE.getOrElse(newAccountColorIndex++) {
                newAccountColorIndex = 0
                IVY_COLOR_PICKER_COLORS_FREE.first()
            }
        }.toArgb()

        val newAccount = Account(
            name = accountNameString,
            currency = mapCurrency(
                baseCurrency = baseCurrency.code,
                currencyCode = currencyRawString
            ),
            color = colorArgb,
            icon = icon,
            orderNum = orderNum ?: accounts.maxOfOrNull { it.orderNum }.nextOrderNum()
        )
        val domainAccount = newAccount.toDomainAccount(baseCurrency).getOrNull()
            ?: return null
        saveAccountUseCase(domainAccount)
        accounts = getLegacyAccountsUseCase()

        return newAccount
    }

    private fun mapCurrency(
        baseCurrency: String,
        currencyCode: String?
    ): String {
        return try {
            if (currencyCode != null && currencyCode.isNotBlank()) {
                IvyCurrency.fromCode(currencyCode)?.code ?: baseCurrency
            } else {
                baseCurrency
            }
        } catch (e: Exception) {
            baseCurrency
        }
    }

    private suspend fun mapCategory(
        categoryNameString: String?,
        color: Int?,
        icon: String?,
        orderNum: Double?
    ): Category? {
        if (categoryNameString == null || categoryNameString.isBlank()) return null

        val existingCategory = categories.firstOrNull {
            categoryNameString.toLowerCaseLocal() == it.name.value.toLowerCaseLocal()
        }
        if (existingCategory != null) {
            return existingCategory
        }

        // create new category
        val colorArgb = color ?: IVY_COLOR_PICKER_COLORS_FREE.getOrElse(newCategoryColorIndex++) {
            newCategoryColorIndex = 0
            IVY_COLOR_PICKER_COLORS_FREE.first()
        }.toArgb()

        val newCategory: Category? = either {
            Category(
                id = CategoryId(UUID.randomUUID()),
                name = NotBlankTrimmedString.from(categoryNameString).bind(),
                color = ColorInt(colorArgb),
                icon = icon?.let(IconAsset::from)?.getOrNull(),
                orderNum = orderNum ?: categories.maxOfOrNull { it.orderNum }.nextOrderNum(),
            )
        }.getOrNull()

        if (newCategory != null) {
            saveCategoryUseCase(newCategory)
            categories = getCategoriesUseCase()
        }

        return newCategory
    }
}
