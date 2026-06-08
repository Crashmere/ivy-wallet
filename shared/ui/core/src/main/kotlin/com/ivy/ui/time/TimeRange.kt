package com.ivy.ui.time

import java.time.Instant
import java.util.concurrent.TimeUnit

internal const val SafeOffsetDays = 365 * 10L

internal val INSTANT_MIN_SAFE: Instant
    get() = Instant.ofEpochMilli(Long.MIN_VALUE)
        .plusSeconds(TimeUnit.DAYS.toSeconds(SafeOffsetDays))

internal val INSTANT_MAX_SAFE: Instant
    get() = Instant.ofEpochMilli(Long.MAX_VALUE)
        .minusSeconds(TimeUnit.DAYS.toSeconds(SafeOffsetDays))
