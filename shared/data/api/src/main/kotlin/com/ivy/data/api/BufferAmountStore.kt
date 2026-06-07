package com.ivy.data.api

import java.math.BigDecimal

interface BufferAmountStore {
    suspend fun getBufferAmount(): BigDecimal

    suspend fun setBufferAmount(amount: BigDecimal): BigDecimal
}
