package com.ivy.ui

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.ivy.domain.features.Features
import com.ivy.ui.time.DevicePreferences
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import javax.inject.Inject
import kotlin.math.abs

const val THOUSAND = 1_000
const val MILLION = 1_000_000
const val BILLION = 1_000_000_000

class FormatMoneyUseCase @Inject constructor(
    private val features: Features,
    private val devicePreferences: DevicePreferences,
    private val featureDataStore: DataStore<Preferences>
) {

    private val locale = devicePreferences.locale()
    private val withoutDecimalFormatter = DecimalFormat("###,###", DecimalFormatSymbols(locale))
    private val withDecimalFormatter = DecimalFormat("###,###.00", DecimalFormatSymbols(locale))
    private val shortenAmountFormatter = DecimalFormat("###,###.##", DecimalFormatSymbols(locale))

    suspend fun format(value: Double, shortenAmount: Boolean): String {
        if (abs(value) >= THOUSAND && shortenAmount) {
            val result = if (abs(value) >= BILLION) {
                "${shortenAmountFormatter.format(value / BILLION)}b"
            } else if (abs(value) >= MILLION) {
                "${shortenAmountFormatter.format(value / MILLION)}m"
            } else {
                "${shortenAmountFormatter.format(value / THOUSAND)}k"
            }
            return result
        } else {
            val showDecimalPoint = features.showDecimalNumber.isEnabled(featureDataStore)

            val formatter = when (showDecimalPoint) {
                true -> withDecimalFormatter
                false -> withoutDecimalFormatter
            }
            return formatter.format(value)
        }
    }
}
