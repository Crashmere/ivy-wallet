package com.ivy.legacy.data.model

import com.ivy.ui.time.TimeFormatter

fun FromToTimeRange.toDisplay(
    timeFormatter: TimeFormatter
): String = with(timeFormatter) {
    val from = from
    val to = to
    val style = TimeFormatter.Style.DateOnly(includeWeekDay = false)
    when {
        from != null && to != null -> {
            "${from.formatLocal(style)} - ${to.formatLocal(style)}"
        }

        from != null && to == null -> {
            "From ${from.formatLocal(style)}"
        }

        from == null && to != null -> {
            "To ${to.formatLocal(style)}"
        }

        else -> {
            "Range"
        }
    }
}
