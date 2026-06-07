package com.ivy.legacy.frp

import com.ivy.legacy.frp.action.Action

inline infix fun <A, B> A.asParamTo(crossinline f: suspend (A) -> B): suspend () -> B = {
    f(this)
}

infix fun <A, B> A.asParamTo(act: Action<A, B>): suspend () -> B = {
    act(this)
}

inline infix fun <B, C> (() -> B).then(crossinline f: suspend (B) -> C): suspend () -> C = {
    val b = this()
    f(b)
}

infix fun <B, C> (() -> B).then(act: Action<B, C>): suspend () -> C = {
    val b = this()
    act(b)
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

infix fun <B, C> (suspend () -> B).then(act: Action<B, C>): suspend () -> C = {
    val b = this()
    act(b)
}

inline infix fun <A, B, C> (suspend (A) -> B).then(
    crossinline f: suspend (B) -> C
): suspend (A) -> C =
    { a ->
        val b = this(a)
        f(b)
    }

infix fun <A, B, C> (suspend (A) -> B).then(act: Action<B, C>): suspend (A) -> C = { a ->
    val b = this(a)
    act(b)
}

inline infix fun <A, B, C> (Action<A, B>).then(crossinline f: suspend (B) -> C): suspend (A) -> C =
    { a ->
        val b = this(a)
        f(b)
    }

infix fun <A, B, C> (Action<A, B>).then(act: Action<B, C>): suspend (A) -> C = { a ->
    val b = this(a)
    act(b)
}

suspend inline infix fun <B, C> (() -> B).thenInvokeAfter(crossinline f: suspend (B) -> C): C {
    val b = this@thenInvokeAfter()
    return f(b)
}

suspend infix fun <B, C> (() -> B).thenInvokeAfter(act: Action<B, C>): C {
    val b = this@thenInvokeAfter()
    return act(b)
}

suspend inline infix fun <B, C> (suspend () -> B).thenInvokeAfter(
    crossinline f: suspend (B) -> C
): C {
    val b = this@thenInvokeAfter()
    return f(b)
}

suspend infix fun <B, C> (suspend () -> B).thenInvokeAfter(act: Action<B, C>): C {
    val b = this@thenInvokeAfter()
    return act(b)
}

suspend inline infix fun <B, C> (Action<Unit, B>).thenInvokeAfter(
    crossinline f: suspend (B) -> C
): C {
    val b = this@thenInvokeAfter(Unit)
    return f(b)
}

suspend infix fun <B, C> (Action<Unit, B>).thenInvokeAfter(act: Action<B, C>): C {
    val b = this@thenInvokeAfter(Unit)
    return act(b)
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

fun <A, B> (Action<A, B>).lambda(): suspend (A) -> B = { a ->
    this(a)
}

fun <B> (Action<Unit, B>).lambda(): suspend () -> B = {
    this(Unit)
}

fun <A> (A).lambda(): suspend () -> A = suspend {
    this
}
