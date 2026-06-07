package com.ivy.domain.usecase.settings

import com.ivy.data.api.NotificationPreferenceStore
import javax.inject.Inject

class SetShowNotificationsPreferenceUseCase @Inject constructor(
    private val notificationPreferenceStore: NotificationPreferenceStore,
) {
    operator fun invoke(enabled: Boolean) {
        notificationPreferenceStore.showNotifications = enabled
    }
}
