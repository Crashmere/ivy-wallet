package com.ivy.domain.usecase.settings

import com.ivy.data.repository.SettingsRepository
import java.math.BigDecimal
import javax.inject.Inject

class SetBufferAmountUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(amount: BigDecimal): BigDecimal {
        return settingsRepository.setBufferAmount(amount)
    }
}
