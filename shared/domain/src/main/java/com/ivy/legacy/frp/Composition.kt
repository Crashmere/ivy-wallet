package com.ivy.legacy.frp

inline infix fun <A, B> A.asParamTo(crossinline f: suspend (A) -> B): suspend () -> B = {
    f(this)
}

inline infix fun <B, C> (() -> B).then(crossinline f: suspend (B) -> C): suspend () -> C = {
    val b = this()
    f(b)
}

inline infix fun <A, B, C> ((A) -> B).then(crossinline f: suspend (B) -> C): suspend (A) -> C =
    { a ->
        val b = this(a)
        f(b)
    }

inline infix fun <B, C> (suspend () -> B).then(crossinline f: suspend (B) -> C): suspend () -> C = {
    val b = this()
    f(b)
}

inline infix fun <A, B, C> (suspend (A) -> B).then(
    crossinline f: suspend (B) -> C
): suspend (A) -> C =
    { a ->
        val b = this(a)
        f(b)
    }

suspend inline infix fun <B, C> (() -> B).thenInvokeAfter(crossinline f: suspend (B) -> C): C {
    val b = this@thenInvokeAfter()
    return f(b)
}

suspend inline infix fun <B, C> (suspend () -> B).thenInvokeAfter(
    crossinline f: suspend (B) -> C
): C {
    val b = this@thenInvokeAfter()
    return f(b)
}

fun <C> (() -> C).fixUnit(): (Unit) -> C = {
    this()
}

fun <C> (suspend () -> C).fixUnit(): suspend (Unit) -> C = {
    this()
}

fun <C> ((Unit) -> C).fixUnit(): () -> C = {
    this(Unit)
}

fun <C> (suspend (Unit) -> C).fixUnit(): suspend () -> C = {
    this(Unit)
}

fun <A> (A).lambda(): suspend () -> A = suspend {
    this
}
