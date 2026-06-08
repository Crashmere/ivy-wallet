package com.ivy.domain.usecase.settings

import com.ivy.data.api.BufferAmountStore
import java.math.BigDecimal
import javax.inject.Inject

class GetBufferAmountUseCase @Inject internal constructor(
    private val bufferAmountStore: BufferAmountStore
) {
    suspend operator fun invoke(): BigDecimal {
        return bufferAmountStore.getBufferAmount()
    }
}
