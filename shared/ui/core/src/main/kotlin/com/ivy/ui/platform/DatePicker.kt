package com.ivy.ui.platform

import androidx.compose.runtime.compositionLocalOf
import java.time.LocalDate

@Suppress("CompositionLocalAllowlist")
val LocalDatePicker = compositionLocalOf<DatePicker> { error("No LocalDatePicker") }

interface DatePicker {
    fun pickDate(
        minDate: LocalDate? = null,
        maxDate: LocalDate? = null,
        initialDate: LocalDate?,
        onDatePicked: (LocalDate) -> Unit
    )
}
