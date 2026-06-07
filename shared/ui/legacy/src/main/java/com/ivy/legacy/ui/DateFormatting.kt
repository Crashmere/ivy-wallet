package com.ivy.legacy.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ivy.ui.R
import com.ivy.ui.time.LocalTimeProvider
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@Deprecated("Use the TimeConverter interface via DI")
@Composable
fun LocalDateTime.formatNicely(
    noWeekDay: Boolean = false,
    zone: ZoneId = ZoneOffset.systemDefault()
): String {
    val today = LocalTimeProvider.current.localDateNow()
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
            stringResource(R.string.today_date, this.formatLocal(patternNoWeekDay, zone))
        }

        today.minusDays(1) -> {
            stringResource(R.string.yesterday_date, this.formatLocal(patternNoWeekDay, zone))
        }

        today.plusDays(1) -> {
            stringResource(R.string.tomorrow_date, this.formatLocal(patternNoWeekDay, zone))
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
@Composable
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

@Composable
fun LocalDate.closeDay(): String? {
    val today = LocalTimeProvider.current.localDateNow()
    return when (this) {
        today -> {
            stringResource(R.string.today)
        }

        today.minusDays(1) -> {
            stringResource(R.string.yesterday)
        }

        today.plusDays(1) -> {
            stringResource(R.string.tomorrow)
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
    return this.toInstant(ZoneOffset.UTC).let { instant ->
        DateTimeFormatter
            .ofPattern(pattern)
            .withLocale(Locale.getDefault())
            .withZone(zone)
            .format(instant)
    }
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
