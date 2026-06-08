package com.ivy.domain.util

internal fun Double?.nextOrderNum(): Double = this?.plus(1) ?: 0.0
