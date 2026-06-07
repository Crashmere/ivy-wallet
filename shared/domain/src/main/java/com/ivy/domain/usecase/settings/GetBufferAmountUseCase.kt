package com.ivy.domain.usecase.settings

import com.ivy.data.repository.LegacySettingsRepository
import java.math.BigDecimal
import javax.inject.Inject

class GetBufferAmountUseCase @Inject constructor(
    private val legacySettingsRepository: LegacySettingsRepository
) {
    suspend operator fun invoke(): BigDecimal {
        return legacySettingsRepository.getBufferAmount()
    }
}
