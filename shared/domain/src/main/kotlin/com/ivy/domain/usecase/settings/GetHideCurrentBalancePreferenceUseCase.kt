package com.ivy.domain.usecase.settings

import com.ivy.data.api.BalancePrivacyPreferenceStore
import javax.inject.Inject

class GetHideCurrentBalancePreferenceUseCase @Inject constructor(
    private val balancePrivacyPreferenceStore: BalancePrivacyPreferenceStore,
) {
    operator fun invoke(): Boolean {
        return balancePrivacyPreferenceStore.hideCurrentBalance
    }
}
