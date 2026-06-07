package com.ivy.data.model.legacy

import java.time.Instant

data class ClosedTimeRange(
    val from: Instant,
    val to: Instant,
) {
    companion object {
        fun allTimeIvy(
            now: Instant,
        ): ClosedTimeRange = ClosedTimeRange(
            from = legacyMinTime(),
            to = now,
        )

        fun to(to: Instant): ClosedTimeRange = ClosedTimeRange(
            from = legacyMinTime(),
            to = to
        )
    }
}
