package com.ivy.base.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun <T> ioThread(action: suspend () -> T): T = withContext(Dispatchers.IO) {
    return@withContext action()
}

suspend fun <T> scopedIOThread(action: suspend (scope: CoroutineScope) -> T): T =
    withContext(Dispatchers.IO) {
        return@withContext action(this)
    }

suspend fun <T> computationThread(action: suspend () -> T): T = withContext(Dispatchers.Default) {
    return@withContext action()
}
