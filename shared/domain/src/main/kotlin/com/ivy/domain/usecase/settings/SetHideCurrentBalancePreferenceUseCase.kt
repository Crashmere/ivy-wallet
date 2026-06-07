package com.ivy.domain.usecase.settings

import com.ivy.data.api.BalancePrivacyPreferenceStore
import javax.inject.Inject

class SetHideCurrentBalancePreferenceUseCase @Inject constructor(
    private val balancePrivacyPreferenceStore: BalancePrivacyPreferenceStore,
) {
    operator fun invoke(enabled: Boolean) {
        balancePrivacyPreferenceStore.hideCurrentBalance = enabled
    }
}
