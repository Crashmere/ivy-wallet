package com.ivy.domain.transaction.legacy

import com.ivy.data.model.legacy.Transaction
import com.ivy.domain.time.todayStartOfLocalDayUtc

fun Iterable<Transaction>.filterUpcomingLegacy(): List<Transaction> {
    val todayStartOfDayUtc = todayStartOfLocalDayUtc()
    return filter {
        // make sure that it's in the future
        it.dueDate != null && it.dueDate!!.isAfter(todayStartOfDayUtc)
    }
}

fun Iterable<Transaction>.filterOverdueLegacy(): List<Transaction> {
    val todayStartOfDayUtc = todayStartOfLocalDayUtc()
    return filter {
        // make sure that it's in the past
        it.dueDate != null && it.dueDate!!.isBefore(todayStartOfDayUtc)
    }
}
