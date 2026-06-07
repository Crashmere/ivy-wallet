package com.ivy.domain.time

import com.ivy.data.model.legacy.Transaction
import java.time.Instant

fun Iterable<Transaction>.filterUpcomingLegacy(): List<Transaction> {
    val todayStartOfDayUtc = todayStartOfLocalDayUtc()
    return filter {
        // make sure that it's in the future
        it.dueDate != null && it.dueDate!!.isAfter(todayStartOfDayUtc)
    }
}

fun Iterable<com.ivy.data.model.Transaction>.filterUpcoming(): List<com.ivy.data.model.Transaction> {
    val todayStartOfDayUTC = todayStartOfUtcDay()

    return filter {
        // make sure that it's in the future
        !it.settled && it.time.isAfter(todayStartOfDayUTC)
    }
}

fun Iterable<Transaction>.filterOverdueLegacy(): List<Transaction> {
    val todayStartOfDayUTC = todayStartOfLocalDayUtc()
    return filter {
        // make sure that it's in the past
        it.dueDate != null && it.dueDate!!.isBefore(todayStartOfDayUTC)
    }
}

fun todayStartOfDayUtc(): Instant = todayStartOfLocalDayUtc()

fun Iterable<com.ivy.data.model.Transaction>.filterOverdue(): List<com.ivy.data.model.Transaction> {
    val todayStartOfDayUTC = todayStartOfUtcDay()

    return filter {
        // make sure that it's in the past
        !it.settled && it.time.isBefore(todayStartOfDayUTC)
    }
}
