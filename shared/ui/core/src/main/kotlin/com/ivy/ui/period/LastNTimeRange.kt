package com.ivy.ui.period

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Composable
import com.ivy.data.model.IntervalType
import com.ivy.data.model.incrementDate
import com.ivy.ui.time.forDisplay
import com.ivy.ui.time.TimeProvider
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
