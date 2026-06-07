package com.ivy.ui.platform

import java.time.LocalDate

interface DatePicker {
    fun pickDate(
        minDate: LocalDate? = null,
        maxDate: LocalDate? = null,
        initialDate: LocalDate?,
        onDatePicked: (LocalDate) -> Unit
    )
}
