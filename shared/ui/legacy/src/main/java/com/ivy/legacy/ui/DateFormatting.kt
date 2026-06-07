package com.ivy.legacy.ui

import com.ivy.base.legacy.convertUTCtoLocal
import com.ivy.base.legacy.dateNowUTC
import com.ivy.base.legacy.stringRes
import com.ivy.ui.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@Deprecated("Use the TimeConverter interface via DI")
fun LocalDateTime.formatNicely(
    noWeekDay: Boolean = false,
    zone: ZoneId = ZoneOffset.systemDefault()
): String {
    val today = dateNowUTC()
    val isThisYear = today.year == this.year

    val patternNoWeekDay = "dd MMM"

    if (noWeekDay) {
        return if (isThisYear) {
            this.formatLocal(patternNoWeekDay)
        } else {
            this.formatLocal("dd MMM, yyyy")
        }
    }

    return when (this.toLocalDate()) {
        today -> {
            stringRes(R.string.today_date, this.formatLocal(patternNoWeekDay, zone))
        }

        today.minusDays(1) -> {
            stringRes(R.string.yesterday_date, this.formatLocal(patternNoWeekDay, zone))
        }

        today.plusDays(1) -> {
            stringRes(R.string.tomorrow_date, this.formatLocal(patternNoWeekDay, zone))
        }

        else -> {
            if (isThisYear) {
                this.formatLocal("EEE, dd MMM", zone)
            } else {
                this.formatLocal("dd MMM, yyyy", zone)
            }
        }
    }
}

@Deprecated("Use the TimeConverter interface via DI")
fun LocalDate.formatDateOnly(): String = this.formatLocal("MMM. dd", ZoneOffset.systemDefault())

@Deprecated("Use the TimeConverter interface via DI")
fun LocalDate.formatDateOnlyWithYear(): String =
    this.formatLocal("dd MMM, yyyy", ZoneOffset.systemDefault())

@Deprecated("Use the TimeConverter interface via DI")
fun LocalDate.formatDateWeekDayLong(): String =
    this.formatLocal("EEEE, dd MMM", ZoneOffset.systemDefault())

@Deprecated("Use the TimeConverter interface via DI")
fun LocalDate.formatNicely(
    pattern: String = "EEE, dd MMM",
    patternNoWeekDay: String = "dd MMM",
    zone: ZoneId = ZoneOffset.systemDefault()
): String {
    val closeDay = closeDay()
    return if (closeDay != null) {
        "$closeDay, ${this.formatLocal(patternNoWeekDay, zone)}"
    } else {
        this.formatLocal(
            pattern,
            zone
        )
    }
}

fun LocalDate.closeDay(): String? {
    val today = dateNowUTC()
    return when (this) {
        today -> {
            stringRes(R.string.today)
        }

        today.minusDays(1) -> {
            stringRes(R.string.yesterday)
        }

        today.plusDays(1) -> {
            stringRes(R.string.tomorrow)
        }

        else -> {
            null
        }
    }
}

fun LocalDateTime.formatLocal(
    pattern: String = "dd MMM yyyy, HH:mm",
    zone: ZoneId = ZoneOffset.systemDefault()
): String {
    val localDateTime = this.convertUTCtoLocal(zone)
    return localDateTime.atZone(zone).format(
        DateTimeFormatter
            .ofPattern(pattern)
            .withLocale(Locale.getDefault())
            .withZone(zone)
    )
}

fun LocalDate.formatLocal(
    pattern: String = "dd MMM yyyy",
    zone: ZoneId = ZoneOffset.systemDefault()
): String {
    return this.format(
        DateTimeFormatter
            .ofPattern(pattern)
            .withLocale(Locale.getDefault())
            .withZone(zone)
    )
}
