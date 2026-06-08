package com.ivy.wallet

import android.content.Context
import android.content.Intent
import androidx.core.content.IntentCompat
import com.ivy.ui.navigation.TransactionRouteType

internal object RootIntentExtras {
    const val EXTRA_ADD_TRANSACTION_TYPE = "add_transaction_type_extra"
}

internal fun Context.createRootIntent(): Intent {
    return Intent(this, RootActivity::class.java)
}

@Suppress("SwallowedException")
internal fun Intent.readAddTransactionTypeExtra(): TransactionRouteType? {
    return try {
        IntentCompat.getSerializableExtra(
            this,
            RootIntentExtras.EXTRA_ADD_TRANSACTION_TYPE,
            TransactionRouteType::class.java
        )
            ?: TransactionRouteType.valueOf(
                getStringExtra(RootIntentExtras.EXTRA_ADD_TRANSACTION_TYPE) ?: ""
            )
    } catch (e: IllegalArgumentException) {
        null
    }
}
