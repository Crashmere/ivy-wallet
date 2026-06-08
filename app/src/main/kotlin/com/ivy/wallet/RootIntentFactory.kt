package com.ivy.wallet

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class RootIntentFactory @Inject constructor(
    @ApplicationContext
    private val context: Context
) {
    fun createRootIntent(): Intent {
        return Intent(context, RootActivity::class.java)
    }
}
