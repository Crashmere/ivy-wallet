package com.ivy.legacy.ui.model.period

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Composable
import com.ivy.base.time.incrementDate
import com.ivy.base.time.TimeProvider
import com.ivy.data.model.IntervalType
import com.ivy.legacy.ui.forDisplay
import java.time.Instant

@Suppress("DataClassFunctions")
@Immutable
data class LastNTimeRange(
    val periodN: Int,
    val periodType: IntervalType,
) {
    fun fromDate(
        timeProvider: TimeProvider
    ): Instant = periodType.incrementDate(
        date = timeProvider.utcNow(),
        intervalN = -periodN.toLong()
    )

    @Composable
    fun forDisplay(): String =
        "$periodN ${periodType.forDisplay(periodN)}"
}
