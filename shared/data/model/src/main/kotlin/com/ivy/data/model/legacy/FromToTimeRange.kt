package com.ivy.data.model.legacy

import java.time.Instant

@Suppress("DataClassFunctions")
data class FromToTimeRange(
    val from: Instant?,
    val to: Instant?,
) {
    fun from(): Instant =
        from ?: legacyMinTime()

    fun to(): Instant =
        to ?: legacyMaxTime()

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
        from = from ?: legacyMinTime(),
        to = to ?: legacyMaxTime()
    )
}

fun FromToTimeRange.toUTCCloseTimeRange(): ClosedTimeRange {
    return ClosedTimeRange(
        from = from ?: legacyMinTime(),
        to = to ?: legacyMaxTime()
    )
}
