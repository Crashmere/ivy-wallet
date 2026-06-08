package com.ivy.domain.transaction.legacy

import com.ivy.data.model.legacy.LegacyTransaction
import com.ivy.domain.time.todayStartOfLocalDayUtc

fun Iterable<LegacyTransaction>.filterUpcomingLegacyTransactions(): List<LegacyTransaction> {
    val todayStartOfDayUtc = todayStartOfLocalDayUtc()
    return filter {
        // make sure that it's in the future
        it.dueDate != null && it.dueDate!!.isAfter(todayStartOfDayUtc)
    }
}

fun Iterable<LegacyTransaction>.filterOverdueLegacyTransactions(): List<LegacyTransaction> {
    val todayStartOfDayUtc = todayStartOfLocalDayUtc()
    return filter {
        // make sure that it's in the past
        it.dueDate != null && it.dueDate!!.isBefore(todayStartOfDayUtc)
    }
}
