package com.ivy.data.model

/**
 * Indicates that the item can be reordered by the user.
 * For the reordering to happen efficiently the item must have an [orderNum].
 */
interface Reorderable {
    val orderNum: Double

    fun withNewOrderNum(newOrderNum: Double): Reorderable {
        error("Reordering is not implemented for ${this::class.simpleName}")
    }
}
