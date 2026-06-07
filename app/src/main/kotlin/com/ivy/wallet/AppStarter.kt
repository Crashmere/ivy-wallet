package com.ivy.wallet

import android.content.Intent
import com.ivy.data.model.TransactionType

interface AppStarter {
    fun getRootIntent(): Intent
    fun defaultStart()
    fun addTransactionStart(type: TransactionType)
}
