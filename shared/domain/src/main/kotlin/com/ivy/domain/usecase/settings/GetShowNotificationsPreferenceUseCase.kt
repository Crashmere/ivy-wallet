package com.ivy.domain.usecase.settings

import com.ivy.data.api.NotificationPreferenceStore
import javax.inject.Inject

class GetShowNotificationsPreferenceUseCase @Inject internal constructor(
    private val notificationPreferenceStore: NotificationPreferenceStore,
) {
    operator fun invoke(): Boolean {
        return notificationPreferenceStore.showNotifications
    }
}
