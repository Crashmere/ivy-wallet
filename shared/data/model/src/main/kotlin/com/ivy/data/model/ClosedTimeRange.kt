package com.ivy.data.model

import java.time.Instant

data class ClosedTimeRange(
    val from: Instant,
    val to: Instant,
) {
    companion object {
        fun allTimeIvy(
            now: Instant,
        ): ClosedTimeRange = ClosedTimeRange(
            from = safeMinTime(),
            to = now,
        )

        fun to(to: Instant): ClosedTimeRange = ClosedTimeRange(
            from = safeMinTime(),
            to = to
        )
    }
}
