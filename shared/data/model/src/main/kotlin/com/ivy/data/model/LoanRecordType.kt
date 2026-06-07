package com.ivy.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class LoanRecordType {
    INCREASE, DECREASE
}

fun <T> LoanRecordType.processByType(decreaseAction: () -> T, increaseAction: () -> T): T {
    return when (this) {
        LoanRecordType.DECREASE -> decreaseAction()
        LoanRecordType.INCREASE -> increaseAction()
    }
}
