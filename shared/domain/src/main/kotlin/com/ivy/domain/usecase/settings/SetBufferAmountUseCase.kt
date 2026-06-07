package com.ivy.domain.usecase.settings

import com.ivy.data.api.BufferAmountStore
import java.math.BigDecimal
import javax.inject.Inject

class SetBufferAmountUseCase @Inject constructor(
    private val bufferAmountStore: BufferAmountStore
) {
    suspend operator fun invoke(amount: BigDecimal): BigDecimal {
        return bufferAmountStore.setBufferAmount(amount)
    }
}
