package com.ivy.importdata.csv.domain

import com.ivy.importdata.csv.CSVRow
import com.ivy.importdata.csv.ColumnMapping
import com.ivy.importdata.csv.MappingStatus

internal const val SAMPLE_SIZE = 20

internal fun <T, M> List<CSVRow>.parseStatus(
    mapping: ColumnMapping<M>,
    parse: (String, M) -> T?
): MappingStatus = tryStatus {
    val parsed = this.mapNotNull {
        parse(it.values[mapping.index], mapping.metadata)
    }

    MappingStatus(
        sampleValues = parsed.map { it.toString() },
        success = parsed.isNotEmpty()
    )
}

private fun tryStatus(block: () -> MappingStatus): MappingStatus = try {
    block()
} catch (e: Exception) {
    MappingStatus(sampleValues = emptyList(), success = false)
}

internal fun mappingFailure(): MappingStatus = MappingStatus(sampleValues = emptyList(), success = false)
