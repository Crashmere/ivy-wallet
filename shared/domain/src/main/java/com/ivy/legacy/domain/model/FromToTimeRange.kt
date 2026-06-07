package com.ivy.legacy.domain.model

import androidx.compose.runtime.Immutable
import com.ivy.base.model.legacy.Transaction
import com.ivy.base.time.TimeConverter
import com.ivy.base.time.TimeProvider
import com.ivy.base.time.ivyMinTime
import com.ivy.base.time.ivyMaxTime
import com.ivy.legacy.domain.pure.data.ClosedTimeRange
import java.time.Instant
import java.time.ZoneOffset

@Suppress("DataClassFunctions")
@Immutable
data class FromToTimeRange(
    val from: Instant?,
    val to: Instant?,
) {
    fun from(): Instant =
        from ?: ivyMinTime()

    fun to(): Instant =
        to ?: ivyMaxTime()

    fun upcomingFrom(
        timeProvider: TimeProvider
    ): Instant {
        val startOfDayNowUTC = timeProvider.utcNow()
        return if (includes(startOfDayNowUTC)) startOfDayNowUTC else from()
    }

    fun overdueTo(
        timeProvider: TimeProvider
    ): Instant {
        val startOfDayNowUTC = timeProvider.utcNow()
        return if (includes(startOfDayNowUTC)) startOfDayNowUTC else to()
    }

    fun includes(dateTime: Instant): Boolean =
        dateTime.isAfter(from()) && dateTime.isBefore(to())
}

@Deprecated("Uses legacy Transaction")
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

@Deprecated("Uses legacy Transaction")
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

fun FromToTimeRange.toCloseTimeRangeUnsafe(): ClosedTimeRange {
    return ClosedTimeRange(
        from = from(),
        to = to()
    )
}

fun FromToTimeRange.toCloseTimeRange(): ClosedTimeRange {
    return ClosedTimeRange(
        from = from ?: ivyMinTime(),
        to = to ?: ivyMaxTime()
    )
}

fun FromToTimeRange.toUTCCloseTimeRange(): ClosedTimeRange {
    return ClosedTimeRange(
        from = from ?: ivyMinTime(),
        to = to ?: ivyMaxTime()
    )
}
