package com.ivy.importdata.csv

import com.ivy.data.model.importing.ImportResult
import kotlinx.collections.immutable.ImmutableList

internal data class CSVState(
    val uiState: UIState,
    val columns: CSVRow?,
    val csv: ImmutableList<CSVRow>?,
    val important: ImportantFields?,
    val transfer: TransferFields?,
    val optional: OptionalFields?,
    val continueEnabled: Boolean,
)

internal sealed interface UIState {
    object Idle : UIState
    data class Processing(val percent: Int) : UIState
    data class Result(val importResult: ImportResult) : UIState
}

internal data class ImportantFields(
    val amount: ColumnMapping<Int>,
    val amountStatus: MappingStatus,
    val type: ColumnMapping<TransactionTypeMetadata>,
    val typeStatus: MappingStatus,
    val date: ColumnMapping<DateMetadata>,
    val dateStatus: MappingStatus,
    val account: ColumnMapping<Unit>,
    val accountStatus: MappingStatus,
    val accountCurrency: ColumnMapping<Unit>,
    val accountCurrencyStatus: MappingStatus,
)

internal data class TransactionTypeMetadata(
    val income: String,
    val expense: String,
    val transfer: String?,
)

internal enum class DateMetadata {
    MonthFirst, DateFirst
}

internal data class TransferFields(
    val toAccount: ColumnMapping<Unit>,
    val toAccountStatus: MappingStatus,
    val toAccountCurrency: ColumnMapping<Unit>,
    val toAccountCurrencyStatus: MappingStatus,
    val toAmount: ColumnMapping<Int>,
    val toAmountStatus: MappingStatus,
)

internal data class OptionalFields(
    val category: ColumnMapping<Unit>,
    val categoryStatus: MappingStatus,
    val title: ColumnMapping<Unit>,
    val titleStatus: MappingStatus,
    val description: ColumnMapping<Unit>,
    val descriptionStatus: MappingStatus,
)

internal data class ColumnMapping<M>(
    val ivyColumn: String,
    val helpInfo: String,
    val name: String,
    val index: Int,
    val metadata: M,
    val required: Boolean,
)

internal data class MappingStatus(
    val sampleValues: List<String>,
    val success: Boolean,
)

internal data class CSVRow(
    val values: List<String>
)
