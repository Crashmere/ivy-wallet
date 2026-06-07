package com.ivy.base

import com.ivy.base.threading.DispatchersProvider
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

object TestDispatchersProvider : DispatchersProvider {
    override val main: CoroutineContext = Dispatchers.Unconfined
    override val io: CoroutineContext = Dispatchers.Unconfined
    override val default: CoroutineContext = Dispatchers.Unconfined
}

val TestCoroutineScope = CoroutineScope(
    Dispatchers.Unconfined + CoroutineName("test")
)
