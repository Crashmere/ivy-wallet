package com.ivy.data.model

import java.time.Instant

@Suppress("DataClassFunctions")
data class FromToTimeRange(
    val from: Instant?,
    val to: Instant?,
) {
    fun from(): Instant =
        from ?: safeMinTime()

    fun to(): Instant =
        to ?: safeMaxTime()

    fun upcomingFrom(
        now: Instant
    ): Instant {
        return if (includes(now)) now else from()
    }

    fun overdueTo(
        now: Instant
    ): Instant {
        return if (includes(now)) now else to()
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
        from = from ?: safeMinTime(),
        to = to ?: safeMaxTime()
    )
}

fun FromToTimeRange.toUTCCloseTimeRange(): ClosedTimeRange {
    return ClosedTimeRange(
        from = from ?: safeMinTime(),
        to = to ?: safeMaxTime()
    )
}
