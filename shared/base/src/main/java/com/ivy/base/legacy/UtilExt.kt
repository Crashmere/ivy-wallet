package com.ivy.base.legacy

import java.util.Random

fun <T> MutableList<T>.swap(fromIndex: Int, toIndex: Int) {
    val from = this[fromIndex]
    val to = this[toIndex]

    this[fromIndex] = to
    this[toIndex] = from
}

fun numberBetween(min: Double, max: Double): Double {
    return Random().nextDouble() * (max - min) + min
}
