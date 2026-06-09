package com.ivy.ui.period

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ivy.ui.R
import java.time.LocalDate

@Immutable
data class Month(
    val monthValue: Int,
) {
    companion object {
        internal fun monthsList(): MutableList<Month> = mutableListOf(
            Month(1),
            Month(2),
            Month(3),
            Month(4),
            Month(5),
            Month(6),
            Month(7),
            Month(8),
            Month(9),
            Month(10),
            Month(11),
            Month(12),
        )

        internal fun fromMonthValue(code: Int): Month =
            monthsList().first { it.monthValue == code }
    }

    internal fun toDate(referenceDate: LocalDate): LocalDate =
        referenceDate.withMonth(monthValue)

    internal fun incrementMonthPeriod(
        increment: Long,
        year: Int,
        referenceDate: LocalDate,
    ): TimePeriod {
        val incrementedMonth = toDate(referenceDate).withYear(year).plusMonths(increment)
        return TimePeriod(
            month = fromMonthValue(incrementedMonth.monthValue),
            year = incrementedMonth.year
        )
    }

}

@Composable
internal fun Month.displayName(): String = when (monthValue) {
    1 -> stringResource(R.string.january)
    2 -> stringResource(R.string.february)
    3 -> stringResource(R.string.march)
    4 -> stringResource(R.string.april)
    5 -> stringResource(R.string.may)
    6 -> stringResource(R.string.june)
    7 -> stringResource(R.string.july)
    8 -> stringResource(R.string.august)
    9 -> stringResource(R.string.september)
    10 -> stringResource(R.string.october)
    11 -> stringResource(R.string.november)
    12 -> stringResource(R.string.december)
    else -> monthValue.toString()
}
