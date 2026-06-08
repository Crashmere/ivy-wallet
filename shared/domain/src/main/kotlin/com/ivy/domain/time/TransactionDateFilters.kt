package com.ivy.domain.time

internal fun Iterable<com.ivy.data.model.Transaction>.filterUpcoming(): List<com.ivy.data.model.Transaction> {
    val todayStartOfDayUTC = todayStartOfUtcDay()

    return filter {
        // make sure that it's in the future
        !it.settled && it.time.isAfter(todayStartOfDayUTC)
    }
}

internal fun Iterable<com.ivy.data.model.Transaction>.filterOverdue(): List<com.ivy.data.model.Transaction> {
    val todayStartOfDayUTC = todayStartOfUtcDay()

    return filter {
        // make sure that it's in the past
        !it.settled && it.time.isBefore(todayStartOfDayUTC)
    }
}
