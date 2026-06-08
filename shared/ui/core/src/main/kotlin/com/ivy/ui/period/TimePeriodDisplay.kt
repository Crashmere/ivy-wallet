package com.ivy.ui.period

import androidx.compose.runtime.Composable
import com.ivy.ui.time.LocalTimeConverter
import com.ivy.ui.time.LocalTimeFormatter
import com.ivy.ui.time.LocalTimeProvider

@Composable
fun TimePeriod.displayShort(startDayOfMonth: Int): String = toDisplayShort(
    startDateOfMonth = startDayOfMonth,
    timeConverter = LocalTimeConverter.current,
    timeProvider = LocalTimeProvider.current,
    timeFormatter = LocalTimeFormatter.current,
)

@Composable
fun TimePeriod.displayLong(startDayOfMonth: Int): String = toDisplayLong(
    startDateOfMonth = startDayOfMonth,
    timeProvider = LocalTimeProvider.current,
    timeConverter = LocalTimeConverter.current,
    timeFormatter = LocalTimeFormatter.current,
)
