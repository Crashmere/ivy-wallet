package com.ivy.domain.model

import com.ivy.data.model.testing.shouldBeApprox
import io.kotest.matchers.shouldBe

internal infix fun StatSummary.shouldBeApprox(other: StatSummary) {
    transactionCount shouldBe other.transactionCount
    values.keys shouldBe other.values.keys
    values.keys.forEach { key ->
        values[key]!!.value shouldBeApprox other.values[key]!!.value
    }
}
