package com.ivy.domain.usecase.settings

import com.ivy.data.repository.SettingsRepository
import java.math.BigDecimal
import javax.inject.Inject

class GetBufferAmountUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(): BigDecimal {
        return settingsRepository.getBufferAmount()
    }
}
