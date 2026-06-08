package com.ivy.wallet

import android.content.Intent
import androidx.core.content.IntentCompat
import com.ivy.data.model.TransactionType

internal object RootIntentExtras {
    const val EXTRA_ADD_TRANSACTION_TYPE = "add_transaction_type_extra"
}

@Suppress("SwallowedException")
internal fun Intent.readAddTransactionTypeExtra(): TransactionType? {
    return try {
        IntentCompat.getSerializableExtra(
            this,
            RootIntentExtras.EXTRA_ADD_TRANSACTION_TYPE,
            TransactionType::class.java
        )
            ?: TransactionType.valueOf(
                getStringExtra(RootIntentExtras.EXTRA_ADD_TRANSACTION_TYPE) ?: ""
            )
    } catch (e: IllegalArgumentException) {
        null
    }
}
