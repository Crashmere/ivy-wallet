package com.ivy.wallet.platform

import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.LocalDate

private const val MILLISECONDS_IN_DAY = 24 * 60 * 60 * 1000

fun ActivityDatePicker.registerMaterialDatePicker(fragmentManager: FragmentManager) {
    registerPicker { minDate,
                     maxDate,
                     initialDate,
                     onDatePicked ->
        val picker =
            MaterialDatePicker.Builder.datePicker()
                .setSelection(
                    if (initialDate != null) {
                        initialDate.toEpochDay() * MILLISECONDS_IN_DAY
                    } else {
                        MaterialDatePicker.todayInUtcMilliseconds()
                    }
                )
                .build()
        picker.show(fragmentManager, "datePicker")
        picker.addOnPositiveButtonClickListener {
            onDatePicked(LocalDate.ofEpochDay(it / MILLISECONDS_IN_DAY))
        }

        if (minDate != null) {
            picker.addOnCancelListener {
                onDatePicked(minDate)
            }
        }

        if (maxDate != null) {
            picker.addOnCancelListener {
                onDatePicked(maxDate)
            }
        }

        if (initialDate != null) {
            picker.addOnCancelListener {
                onDatePicked(initialDate)
            }
        }
    }
}
