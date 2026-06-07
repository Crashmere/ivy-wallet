package com.ivy.wallet.notification.reminder

import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ivy.base.resource.ResourceProvider
import com.ivy.domain.AppStarter
import com.ivy.domain.usecase.settings.GetShowNotificationsPreferenceUseCase
import com.ivy.domain.usecase.transaction.CountTodayTransactionsUseCase
import com.ivy.ui.R
import com.ivy.wallet.android.notification.IvyNotificationChannel
import com.ivy.wallet.android.notification.NotificationService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class TransactionReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val countTodayTransactionsUseCase: CountTodayTransactionsUseCase,
    private val notificationService: NotificationService,
    private val getShowNotificationsPreference: GetShowNotificationsPreferenceUseCase,
    private val appStarter: AppStarter,
    private val resourceProvider: ResourceProvider,
) : CoroutineWorker(appContext, params) {

    companion object {
        const val MINIMUM_TRANSACTIONS_PER_DAY = 1
    }

    override suspend fun doWork() = withContext(Dispatchers.IO) {
        val transactionsTodayCount = countTodayTransactionsUseCase()

        val showNotifications = fetchShowNotifications()

        // Double check is needed because the user can switch off notifications in settings after it has been scheduled to show notifications for the next day
        if (transactionsTodayCount.value < MINIMUM_TRANSACTIONS_PER_DAY && showNotifications) {
            // Have less than 1 transaction today, remind them

            val notification = notificationService
                .defaultIvyNotification(
                    channel = IvyNotificationChannel.TRANSACTION_REMINDER,
                    priority = NotificationCompat.PRIORITY_HIGH
                )
                .setContentTitle("Ivy Wallet")
                .setContentText(randomText())
                .setContentIntent(
                    PendingIntent.getActivity(
                        applicationContext,
                        1,
                        appStarter.getRootIntent(),
                        PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_UPDATE_CURRENT
                                or PendingIntent.FLAG_IMMUTABLE
                    )
                )

            notificationService.showNotification(notification, 1)
        }

        return@withContext Result.success()
    }

    private fun randomText(): String =
        listOf(
            resourceProvider.getString(R.string.notification_1),
            resourceProvider.getString(R.string.notification_2),
            resourceProvider.getString(R.string.notification_3),
        ).shuffled().first()

    private fun fetchShowNotifications(): Boolean =
        getShowNotificationsPreference()
}
