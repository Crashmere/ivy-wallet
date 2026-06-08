package com.ivy.importdata.csv.domain

import com.ivy.data.model.legacy.LegacyTransaction
import com.ivy.data.model.TransactionType
import com.ivy.data.model.Category
import com.ivy.data.model.CategoryId
import com.ivy.data.model.importing.ImportCsvRow
import com.ivy.data.model.importing.ImportResult
import com.ivy.data.model.primitive.AssetCode
import com.ivy.data.model.primitive.ColorInt
import com.ivy.data.model.primitive.IconAsset
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.domain.usecase.account.SaveLegacyAccountUseCase
import com.ivy.domain.usecase.category.GetCategoriesUseCase
import com.ivy.domain.usecase.category.SaveCategoryUseCase
import com.ivy.domain.usecase.currency.GetBaseCurrencyUseCase
import com.ivy.domain.usecase.transaction.SaveLegacyTransactionUseCase
import com.ivy.importdata.csv.ImportantFields
import com.ivy.importdata.csv.OptionalFields
import com.ivy.importdata.csv.TransferFields
import com.ivy.data.model.legacy.LegacyAccount
import com.ivy.data.model.currency.IvyCurrency
import com.ivy.domain.usecase.account.GetLegacyAccountsUseCase
import kotlinx.collections.immutable.toImmutableList
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlin.math.absoluteValue
import com.ivy.importdata.csv.CSVRow

internal class CsvTransactionImporter @Inject internal constructor(
    private val getLegacyAccountsUseCase: GetLegacyAccountsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getBaseCurrency: GetBaseCurrencyUseCase,
    private val saveLegacyAccountUseCase: SaveLegacyAccountUseCase,
    private val saveCategoryUseCase: SaveCategoryUseCase,
    private val saveLegacyTransactionUseCase: SaveLegacyTransactionUseCase,
) {
    suspend fun import(
        csv: List<CSVRow>,
        importantFields: ImportantFields,
        transferFields: TransferFields,
        optionalFields: OptionalFields,
        onProgress: suspend (progressPercent: Double) -> Unit,
    ): ImportResult {
        val rows = csv.drop(1) // drop the header
        val rowsCount = rows.size

        val context = CsvImportContext(
            accounts = getLegacyAccountsUseCase(),
            categories = getCategoriesUseCase(),
            baseCurrency = getBaseCurrency(),
        )
        val initialAccountsCount = context.accounts.size
        val initialCategoriesCount = context.categories.size

        val failedRows = mutableListOf<ImportCsvRow>()

        val transactions = rows.mapIndexedNotNull { index, row ->
            val progressPercent = if (rowsCount > 0) {
                index / rowsCount.toDouble()
            } else {
                0.0
            }
            onProgress(progressPercent / 2)

            val transaction = mapToTransaction(
                context = context,
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
            accountsImported = context.accounts.size - initialAccountsCount,
            categoriesImported = context.categories.size - initialCategoriesCount,
            failedRows = failedRows.toImmutableList()
        )
    }

    private suspend fun mapToTransaction(
        context: CsvImportContext,
        row: CSVRow,
        importantFields: ImportantFields,
        transferFields: TransferFields,
        optionalFields: OptionalFields,
    ): LegacyTransaction? {
        val type = parseTransactionType(
            value = row.extractValue(importantFields.type),
            metadata = importantFields.type.metadata,
        ) ?: return null

        val toAccount = if (type == TransactionType.TRANSFER) {
            mapAccount(
                context = context,
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
            context = context,
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
            context = context,
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

        return LegacyTransaction(
            id = UUID.randomUUID(),
            type = type,
            amount = amount.toBigDecimal(),
            accountId = account.id,
            toAccountId = toAccount?.id,
            toAmount = toAmount?.toBigDecimal() ?: amount.toBigDecimal(),
            dateTime = dateTime.toInstantInSystemZone(),
            dueDate = null,
            categoryId = category?.id?.value,
            title = title,
            description = description
        )
    }

    private suspend fun mapAccount(
        context: CsvImportContext,
        accountNameString: String?,
        color: Int?,
        icon: String?,
        orderNum: Double?,
        currencyRawString: String?,
    ): LegacyAccount? {
        if (accountNameString == null || accountNameString.isBlank()) return null

        val existingAccount = context.accounts.firstOrNull {
            accountNameString.lowercase(Locale.getDefault()) == it.name.lowercase(Locale.getDefault())
        }
        if (existingAccount != null) {
            return existingAccount
        }

        // create new account
        val colorArgb = color ?: when {
            accountNameString.lowercase(Locale.getDefault()).contains("cash") -> {
                cashAccountColor
            }

            accountNameString.lowercase(Locale.getDefault()).contains("revolut") -> {
                revolutAccountColor
            }

            else -> defaultImportColorPalette.getOrElse(context.newAccountColorIndex++) {
                context.newAccountColorIndex = 0
                defaultImportColorPalette.first()
            }
        }

        val newAccount = LegacyAccount(
            name = accountNameString,
            currency = mapCurrency(
                baseCurrency = context.baseCurrency.code,
                currencyCode = currencyRawString
            ),
            color = colorArgb,
            icon = icon,
            orderNum = orderNum ?: context.accounts.maxOfOrNull { it.orderNum }.nextImportOrderNum()
        )
        val accountSaved = saveLegacyAccountUseCase(newAccount, context.baseCurrency)
        if (!accountSaved) return null
        context.accounts = getLegacyAccountsUseCase()

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
        context: CsvImportContext,
        categoryNameString: String?,
        color: Int?,
        icon: String?,
        orderNum: Double?
    ): Category? {
        if (categoryNameString == null || categoryNameString.isBlank()) return null

        val existingCategory = context.categories.firstOrNull {
            categoryNameString.lowercase(Locale.getDefault()) == it.name.value.lowercase(Locale.getDefault())
        }
        if (existingCategory != null) {
            return existingCategory
        }

        // create new category
        val colorArgb = color ?: defaultImportColorPalette.getOrElse(context.newCategoryColorIndex++) {
            context.newCategoryColorIndex = 0
            defaultImportColorPalette.first()
        }

        val categoryName = NotBlankTrimmedString.from(categoryNameString).getOrNull()
            ?: return null
        val newCategory = Category(
            id = CategoryId(UUID.randomUUID()),
            name = categoryName,
            color = ColorInt(colorArgb),
            icon = icon?.let(IconAsset::from)?.getOrNull(),
            orderNum = orderNum ?: context.categories.maxOfOrNull { it.orderNum }.nextImportOrderNum(),
        )

        saveCategoryUseCase(newCategory)
        context.categories = getCategoriesUseCase()

        return newCategory
    }

    private fun LocalDateTime.toInstantInSystemZone() =
        atZone(ZoneId.systemDefault()).toInstant()

    private fun Double?.nextImportOrderNum(): Double = this?.plus(1) ?: 0.0
}

private data class CsvImportContext(
    var accounts: List<LegacyAccount>,
    var categories: List<Category>,
    val baseCurrency: AssetCode,
    var newCategoryColorIndex: Int = 0,
    var newAccountColorIndex: Int = 0,
)
