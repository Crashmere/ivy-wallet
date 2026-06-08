package com.ivy.ui.period

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Composable
import com.ivy.data.model.FromToTimeRange
import com.ivy.ui.time.TimeConverter
import com.ivy.ui.time.TimeFormatter
import com.ivy.ui.time.TimeProvider
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

private const val MonthNameAbbreviationLength = 3

private fun startOfMonth(date: LocalDate, timeConverter: TimeConverter): Instant {
    val startOfMonthLocal = date.withDayOfMonth(1).atStartOfDay()
    return with(timeConverter) { startOfMonthLocal.toUTC() }
}

private fun endOfMonth(date: LocalDate, timeConverter: TimeConverter): Instant {
    val endOfMonthLocal = date.withDayOfMonth(date.lengthOfMonth()).atTime(LocalTime.MAX)
    return with(timeConverter) { endOfMonthLocal.toUTC() }
}

private fun LocalDate.atEndOfDay(): LocalDateTime =
    atTime(23, 59, 59)

private fun LocalDate.withDayOfMonthSafe(targetDayOfMonth: Int): LocalDate {
    val maxDayOfMonth = lengthOfMonth()
    return withDayOfMonth(
        if (targetDayOfMonth > maxDayOfMonth) maxDayOfMonth else targetDayOfMonth
    )
}

@Suppress("DataClassFunctions")
@Immutable
data class TimePeriod(
    val month: Month? = null,
    val year: Int? = null,
    val fromToRange: FromToTimeRange? = null,
    val lastNRange: LastNTimeRange? = null,
) {
    companion object {
        /**
         * Examples:
         * 1. startDateOfMonth = 1, today = Nov. 10
         * return Nov. 1 - Nov. 30
         *
         * 2. startDateOfMonth = 10, today = Nov. 9
         * return Oct. 10 - Nov. 9
         *
         * 3. startDateOfMonth = 10, today = Nov. 10
         * return Nov. 10 - Dec. 9
         */
        fun currentMonth(
            startDayOfMonth: Int,
            currentDate: LocalDate,
        ): TimePeriod {
            val dayToday = currentDate.dayOfMonth

            // Examples month = Nov. startDate = 7; Period = from Nov (7) till Dec (6)
            // => new period starts if today => startDayOfMonth
            val newPeriodStarted = dayToday >= startDayOfMonth

            val periodDate = if (newPeriodStarted) {
                // new monthly period has already started then observe it => current month
                currentDate
            } else {
                // new monthly period hasn't yet started then observe the ongoing one => previous month
                currentDate.minusMonths(1)
            }

            return TimePeriod(
                month = Month.fromMonthValue(
                    periodDate.monthValue
                ),
                year = periodDate.year
            )
        }

        fun currentMonth(
            startDayOfMonth: Int,
            timeProvider: TimeProvider,
        ): TimePeriod = currentMonth(
            startDayOfMonth = startDayOfMonth,
            currentDate = timeProvider.localDateNow(),
        )
    }

    fun isValid(): Boolean =
        month != null || fromToRange != null || lastNRange != null

    fun toRange(
        startDateOfMonth: Int,
        timeConverter: TimeConverter,
        timeProvider: TimeProvider,
    ): FromToTimeRange = with(timeConverter) {
        when {
            month != null -> {
                val currentDate = timeProvider.localDateNow()
                val monthDate = month.toDate(currentDate)
                val date = if (year != null) monthDate.withYear(year) else monthDate
                val (from, to) = if (startDateOfMonth != 1) {
                    customStartDayOfMonthPeriodRange(
                        date = date,
                        startDateOfMonth = startDateOfMonth,
                        timeConverter = timeConverter,
                    )
                } else {
                    Pair(startOfMonth(date, timeConverter), endOfMonth(date, timeConverter))
                }

                FromToTimeRange(
                    from = from,
                    to = to
                )
            }

            fromToRange != null -> {
                fromToRange
            }

            lastNRange != null -> {
                FromToTimeRange(
                    from = lastNRange.fromDate(timeProvider),
                    to = timeProvider.utcNow()
                )
            }

            else -> {
                val date = timeProvider.localDateNow()
                FromToTimeRange(
                    from = startOfMonth(date, timeConverter),
                    to = endOfMonth(date, timeConverter)
                )
            }
        }
    }

    private fun customStartDayOfMonthPeriodRange(
        date: LocalDate,
        startDateOfMonth: Int,
        timeConverter: TimeConverter,
    ): Pair<Instant, Instant> = with(timeConverter) {
        val from = date
            .withDayOfMonthSafe(startDateOfMonth)
            .atStartOfDay()
            .toUTC()

        val to = date
            // startDayOfMonth != 1 just shift N day the month forward so to should +1 month
            .plusMonths(1)
            .withDayOfMonthSafe(startDateOfMonth)
            // e.g. Correct: 14.10-13.11 (Incorrect: 14.10-14.11)
            .minusDays(1)
            .atEndOfDay()
            .toUTC()

        from to to
    }

    @Composable
    fun toDisplayShort(
        startDateOfMonth: Int,
        timeConverter: TimeConverter,
        timeProvider: TimeProvider,
        timeFormatter: TimeFormatter,
    ): String = with(timeFormatter) {
        when {
            month != null -> {
                if (startDateOfMonth == 1) {
                    displayMonthStartingOn1st(month = month, timeProvider)
                } else {
                    val range = toRange(
                        startDateOfMonth = startDateOfMonth,
                        timeConverter = timeConverter,
                        timeProvider = timeProvider,
                    )
                    val style = TimeFormatter.Style.DateOnly(includeWeekDay = false)
                    "${range.from?.formatLocal(style)} - ${range.to?.formatLocal(style)}"
                }
            }

            fromToRange != null -> {
                fromToRange.toDisplay(timeFormatter)
            }

            lastNRange != null -> {
                "Last ${lastNRange.forDisplay()}"
            }

            else -> "Custom"
        }
    }

    @Composable
    fun toDisplayLong(
        startDateOfMonth: Int,
        timeProvider: TimeProvider,
        timeConverter: TimeConverter,
        timeFormatter: TimeFormatter
    ): String {
        return when {
            month != null -> {
                if (startDateOfMonth == 1) {
                    displayMonthStartingOn1st(month = month, timeProvider)
                } else {
                    toRange(
                        startDateOfMonth = startDateOfMonth,
                        timeConverter = timeConverter,
                        timeProvider = timeProvider
                    ).toDisplay(timeFormatter)
                }
            }

            fromToRange != null -> {
                fromToRange.toDisplay(timeFormatter)
            }

            lastNRange != null -> {
                "the last ${lastNRange.forDisplay()}"
            }

            else -> {
                toRange(
                    startDateOfMonth = startDateOfMonth,
                    timeConverter = timeConverter,
                    timeProvider = timeProvider
                ).toDisplay(timeFormatter)
            }
        }
    }

    @Composable
    private fun displayMonthStartingOn1st(
        month: Month,
        timeProvider: TimeProvider,
    ): String {
        val monthName = month.displayName()
        val year = year
        return if (year != null && timeProvider.localNow().year != year) {
            // not this year
            "${monthName.take(MonthNameAbbreviationLength)}, $year"
        } else {
            // this year
            monthName
        }
    }
}
