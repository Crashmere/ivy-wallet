package com.ivy.domain.time

import com.ivy.data.model.legacy.Transaction
import com.ivy.base.time.TimeConverter
import com.ivy.base.time.TimeProvider
import java.time.Instant
import java.time.ZoneOffset

fun Iterable<Transaction>.filterUpcomingLegacy(
    timeProvider: TimeProvider,
    timeConverter: TimeConverter,
): List<Transaction> {
    val todayStartOfDayUtc = todayStartOfDayUtc(timeProvider, timeConverter)
    return filter {
        // make sure that it's in the future
        it.dueDate != null && it.dueDate!!.isAfter(todayStartOfDayUtc)
    }
}

fun Iterable<com.ivy.data.model.Transaction>.filterUpcoming(
    timeProvider: TimeProvider,
): List<com.ivy.data.model.Transaction> {
    val todayStartOfDayUTC = todayStartOfUtcDay(timeProvider)

    return filter {
        // make sure that it's in the future
        !it.settled && it.time.isAfter(todayStartOfDayUTC)
    }
}

fun Iterable<Transaction>.filterOverdueLegacy(
    timeProvider: TimeProvider,
    timeConverter: TimeConverter,
): List<Transaction> {
    val todayStartOfDayUTC = todayStartOfDayUtc(timeProvider, timeConverter)
    return filter {
        // make sure that it's in the past
        it.dueDate != null && it.dueDate!!.isBefore(todayStartOfDayUTC)
    }
}

fun todayStartOfDayUtc(
    timeProvider: TimeProvider,
    timeConverter: TimeConverter,
): Instant = with(timeConverter) {
    timeProvider.localNow()
        .withHour(0)
        .withMinute(0)
        .withSecond(0)
        .toUTC()
}

fun Iterable<com.ivy.data.model.Transaction>.filterOverdue(
    timeProvider: TimeProvider,
): List<com.ivy.data.model.Transaction> {
    val todayStartOfDayUTC = todayStartOfUtcDay(timeProvider)

    return filter {
        // make sure that it's in the past
        !it.settled && it.time.isBefore(todayStartOfDayUTC)
    }
}

private fun todayStartOfUtcDay(timeProvider: TimeProvider): Instant =
    timeProvider.utcNow()
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .atStartOfDay()
        .toInstant(ZoneOffset.UTC)
