package com.ivy.domain.usecase.settings

import com.ivy.data.api.SettingsStore
import java.math.BigDecimal
import javax.inject.Inject

class SetBufferAmountUseCase @Inject constructor(
    private val settingsStore: SettingsStore
) {
    suspend operator fun invoke(amount: BigDecimal): BigDecimal {
        return settingsStore.setBufferAmount(amount)
    }
}
