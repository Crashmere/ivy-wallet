package com.ivy.data.model.legacy

import java.time.Instant
import java.util.concurrent.TimeUnit

private const val SafeOffsetDays = 365 * 10L

internal fun legacyMinTime(): Instant =
    Instant.ofEpochMilli(Long.MIN_VALUE)
        .plusSeconds(TimeUnit.DAYS.toSeconds(SafeOffsetDays))

internal fun legacyMaxTime(): Instant =
    Instant.ofEpochMilli(Long.MAX_VALUE)
        .minusSeconds(TimeUnit.DAYS.toSeconds(SafeOffsetDays))
