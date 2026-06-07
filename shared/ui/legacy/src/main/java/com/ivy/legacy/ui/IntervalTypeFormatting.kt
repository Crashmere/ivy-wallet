package com.ivy.legacy.ui

import com.ivy.base.legacy.stringRes
import com.ivy.data.model.IntervalType
import com.ivy.ui.R

fun IntervalType.forDisplay(intervalN: Int): String {
    val plural = intervalN > 1 || intervalN == 0
    return when (this) {
        IntervalType.DAY -> if (plural) stringRes(R.string.days) else stringRes(R.string.day)
        IntervalType.WEEK -> if (plural) stringRes(R.string.weeks) else stringRes(R.string.week)
        IntervalType.MONTH -> if (plural) stringRes(R.string.months) else stringRes(R.string.month)
        IntervalType.YEAR -> if (plural) stringRes(R.string.years) else stringRes(R.string.year)
    }
}
