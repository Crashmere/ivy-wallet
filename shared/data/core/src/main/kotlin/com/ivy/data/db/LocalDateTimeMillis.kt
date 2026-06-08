package com.ivy.data.db

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

internal fun Long.epochMilliToUtcLocalDateTime(): LocalDateTime =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDateTime()

internal fun LocalDateTime.toUtcEpochMilli(): Long =
    this.toInstant(ZoneOffset.UTC).toEpochMilli()
