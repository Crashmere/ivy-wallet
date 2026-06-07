package com.ivy.ui.platform

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import com.ivy.ui.resource.ResourceProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class Toaster @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val resourceProvider: ResourceProvider,
) {
    suspend fun show(@StringRes messageId: Int, duration: Int = Toast.LENGTH_LONG) {
        show(resourceProvider.getString(messageId), duration)
    }

    suspend fun show(message: String, duration: Int = Toast.LENGTH_LONG) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, message, duration).show()
        }
    }
}
