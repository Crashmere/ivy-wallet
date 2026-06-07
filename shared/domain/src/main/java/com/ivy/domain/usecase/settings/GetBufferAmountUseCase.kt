package com.ivy.domain.usecase.settings

import com.ivy.data.api.SettingsStore
import java.math.BigDecimal
import javax.inject.Inject

class GetBufferAmountUseCase @Inject constructor(
    private val settingsStore: SettingsStore
) {
    suspend operator fun invoke(): BigDecimal {
        return settingsStore.getBufferAmount()
    }
}
