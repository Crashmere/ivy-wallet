package com.ivy.data.model.legacy

import com.ivy.base.time.TimeProvider
import com.ivy.base.time.ivyMaxTime
import com.ivy.base.time.ivyMinTime
import java.time.Instant

@Suppress("DataClassFunctions")
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
