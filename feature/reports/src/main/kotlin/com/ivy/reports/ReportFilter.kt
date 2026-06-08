package com.ivy.reports

import com.ivy.data.model.TransactionType
import com.ivy.data.model.Category
import com.ivy.data.model.TagId
import com.ivy.ui.period.TimePeriod
import com.ivy.data.model.legacy.LegacyAccount
import java.util.UUID

data class ReportFilter(
    val id: UUID = UUID.randomUUID(),
    val transactionTypes: List<TransactionType>,
    val period: TimePeriod?,
    val accounts: List<LegacyAccount>,
    val categories: List<Category>,
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
            accounts = emptyList(),
            categories = emptyList(),
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

        if (accounts.isEmpty()) return false

        if (categories.isEmpty()) return false

        if (minAmount != null && maxAmount != null) {
            if (minAmount > maxAmount) return false
            if (maxAmount < minAmount) return false
        }

        return true
    }
}
