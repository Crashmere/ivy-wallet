package com.ivy.wallet.platform

import com.ivy.ui.platform.DatePicker
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ActivityDatePicker @Inject constructor() : DatePicker {
    private var picker: ((
        minDate: LocalDate?,
        maxDate: LocalDate?,
        initialDate: LocalDate?,
        onDatePicked: (LocalDate) -> Unit
    ) -> Unit)? = null

    internal fun registerPicker(
        picker: (
            minDate: LocalDate?,
            maxDate: LocalDate?,
            initialDate: LocalDate?,
            onDatePicked: (LocalDate) -> Unit
        ) -> Unit
    ) {
        this.picker = picker
    }

    override fun pickDate(
        minDate: LocalDate?,
        maxDate: LocalDate?,
        initialDate: LocalDate?,
        onDatePicked: (LocalDate) -> Unit
    ) {
        val picker = picker ?: error("Date picker is not registered")
        picker(minDate, maxDate, initialDate, onDatePicked)
    }
}
