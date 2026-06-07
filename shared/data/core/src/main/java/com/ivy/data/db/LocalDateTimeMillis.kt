package com.ivy.data.db

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

fun Long.epochMilliToUtcLocalDateTime(): LocalDateTime =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDateTime()

fun LocalDateTime.toUtcEpochMilli(): Long =
    this.toInstant(ZoneOffset.UTC).toEpochMilli()
