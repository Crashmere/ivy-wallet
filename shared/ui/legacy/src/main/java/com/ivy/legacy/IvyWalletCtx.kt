package com.ivy.legacy

import com.ivy.design.IvyContext
import javax.inject.Inject
import javax.inject.Singleton

@Deprecated("Legacy code. Don't use it, please.")
@Singleton
class IvyWalletCtx @Inject constructor() : IvyContext()
