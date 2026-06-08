package com.ivy.reports

import com.ivy.data.model.CategoryId
import com.ivy.data.model.TransactionType
import com.ivy.data.model.TagId
import com.ivy.ui.period.TimePeriod
import java.util.UUID

internal data class ReportFilter(
    val id: UUID = UUID.randomUUID(),
    val transactionTypes: List<TransactionType>,
    val period: TimePeriod?,
    val accountIds: List<UUID>,
    val categoryIds: List<CategoryId>,
    val currency: String,
    val minAmount: Double?,
    val maxAmount: Double?,
    val includeKeywords: List<String>,
    val excludeKeywords: List<String>,
    val includedTags: List<TagId>,
    val excludedTags: List<TagId>,

) {
    companion object {
        fun emptyFilter(
            baseCurrency: String
        ) = ReportFilter(
            transactionTypes = emptyList(),
            period = null,
            accountIds = emptyList(),
            categoryIds = emptyList(),
            currency = baseCurrency,
            includeKeywords = emptyList(),
            excludeKeywords = emptyList(),
            minAmount = null,
            maxAmount = null,
            includedTags = emptyList(),
            excludedTags = emptyList()
        )
    }

    fun validate(): Boolean {
        if (transactionTypes.isEmpty()) return false

        if (period == null) return false

        if (accountIds.isEmpty()) return false

        if (categoryIds.isEmpty()) return false

        if (minAmount != null && maxAmount != null) {
            if (minAmount > maxAmount) return false
            if (maxAmount < minAmount) return false
        }

        return true
    }
}
