package com.ivy.base.collections

fun <T> MutableList<T>.swap(fromIndex: Int, toIndex: Int) {
    val from = this[fromIndex]
    val to = this[toIndex]

    this[fromIndex] = to
    this[toIndex] = from
}
