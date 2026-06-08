package com.ivy.domain.model

import com.ivy.data.model.primitive.AssetCode
import com.ivy.data.model.primitive.NonNegativeInt
import com.ivy.data.model.primitive.PositiveDouble

internal data class StatSummary(
    val transactionCount: NonNegativeInt,
    val values: Map<AssetCode, PositiveDouble>,
) {
    companion object {
        val Zero = StatSummary(
            values = emptyMap(),
            transactionCount = NonNegativeInt.Zero
        )
    }
}
