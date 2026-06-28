package com.ivy.loans

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

internal fun nowUtc(): Instant = Instant.now()

internal fun nowLocalDate(): LocalDate = LocalDate.now()

internal fun nowLocalTime(): LocalTime = LocalTime.now()

internal fun LocalDateTime.toUtcInstant(): Instant =
    atZone(ZoneId.systemDefault()).toInstant()

internal fun Instant.toLocalDateInSystemZone(): LocalDate =
    atZone(ZoneId.systemDefault()).toLocalDate()

internal fun Instant.toLocalTimeInSystemZone(): LocalTime =
    atZone(ZoneId.systemDefault()).toLocalTime()
