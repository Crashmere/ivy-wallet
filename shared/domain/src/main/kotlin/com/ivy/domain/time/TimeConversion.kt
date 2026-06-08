package com.ivy.domain.time

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

private const val SAFE_OFFSET_DAYS = 365 * 10L

internal val DOMAIN_INSTANT_MIN_SAFE: Instant
    get() = Instant.ofEpochMilli(Long.MIN_VALUE)
        .plusSeconds(TimeUnit.DAYS.toSeconds(SAFE_OFFSET_DAYS))

internal fun nowUtc(): Instant = Instant.now()

internal fun nowLocalDate(): LocalDate = LocalDate.now()

internal fun todayStartOfUtcDay(): Instant =
    nowUtc()
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .atStartOfDay()
        .toInstant(ZoneOffset.UTC)

internal fun todayStartOfLocalDayUtc(): Instant =
    nowLocalDate()
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()

internal fun LocalDateTime.toUtcInstant(): Instant =
    atZone(ZoneId.systemDefault()).toInstant()

internal fun Instant.toLocalDateInSystemZone(): LocalDate =
    atZone(ZoneId.systemDefault()).toLocalDate()

internal fun Instant.toLocalDateTimeInSystemZone(): LocalDateTime =
    atZone(ZoneId.systemDefault()).toLocalDateTime()

internal fun Instant.convertToLocal(): ZonedDateTime {
    return atZone(ZoneId.systemDefault())
}
