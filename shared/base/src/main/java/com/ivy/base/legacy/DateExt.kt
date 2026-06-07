package com.ivy.base.legacy

import com.ivy.base.time.INSTANT_MAX_SAFE
import com.ivy.base.time.INSTANT_MIN_SAFE
import com.ivy.base.time.TimeConverter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Deprecated("Use the TimeProvider interface via DI")
fun timeNowLocal(): LocalDateTime = LocalDateTime.now()

@Deprecated("Use the TimeProvider interface via DI")
fun dateNowLocal(): LocalDate = LocalDate.now()

@Deprecated("Use the TimeProvider interface via DI")
fun timeNowUTC(): LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)

@Deprecated("Use the TimeProvider interface via DI")
fun dateNowUTC(): LocalDate = LocalDate.now(ZoneOffset.UTC)

fun LocalDateTime.toEpochSeconds() = this.toEpochSecond(ZoneOffset.UTC)

fun LocalDateTime.getISOFormattedDateTime(): String =
    this.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"))

fun LocalDateTime.format(
    pattern: String
): String {
    return this.format(
        DateTimeFormatter.ofPattern(pattern)
    )
}

@Deprecated("Use the TimeConverter interface via DI")
fun LocalDateTime.convertUTCtoLocal(zone: ZoneId = ZoneOffset.systemDefault()): LocalDateTime {
    return this.convertUTCto(zone)
}

@Deprecated("Use the TimeConverter interface via DI")
fun LocalDateTime.convertUTCto(zone: ZoneId): LocalDateTime {
    return plusSeconds(atZone(zone).offset.totalSeconds.toLong())
}

@Deprecated("Use the TimeConverter interface via DI")
fun LocalTime.convertLocalToUTC(): LocalTime {
    val offset = timeNowLocal().atZone(ZoneOffset.systemDefault()).offset.totalSeconds.toLong()
    return this.minusSeconds(offset)
}

@Deprecated("Use the TimeConverter interface via DI")
fun LocalTime.convertUTCToLocal(): LocalTime {
    val offset = timeNowLocal().atZone(ZoneOffset.systemDefault()).offset.totalSeconds.toLong()
    return this.plusSeconds(offset)
}

@Deprecated("Use the TimeConverter interface via DI")
fun LocalDateTime.convertLocalToUTC(): LocalDateTime {
    val offset = timeNowLocal().atZone(ZoneOffset.systemDefault()).offset.totalSeconds.toLong()
    return this.minusSeconds(offset)
}

fun startOfMonth(date: LocalDate, timeConverter: TimeConverter): Instant {
    val startOfMonthLocal = date.withDayOfMonth(1).atStartOfDay()
    return with(timeConverter) { startOfMonthLocal.toUTC() }
}

fun endOfMonth(date: LocalDate, timeConverter: TimeConverter): Instant {
    val endOfMonthLocal = date.withDayOfMonth(date.lengthOfMonth()).atTime(LocalTime.MAX)
    return with(timeConverter) { endOfMonthLocal.toUTC() }
}

fun LocalDate.atEndOfDay(): LocalDateTime =
    this.atTime(23, 59, 59)

fun ivyMinTime(): Instant = INSTANT_MIN_SAFE

fun ivyMaxTime(): Instant = INSTANT_MAX_SAFE

fun LocalDate.withDayOfMonthSafe(targetDayOfMonth: Int): LocalDate {
    val maxDayOfMonth = this.lengthOfMonth()
    return this.withDayOfMonth(
        if (targetDayOfMonth > maxDayOfMonth) maxDayOfMonth else targetDayOfMonth
    )
}
