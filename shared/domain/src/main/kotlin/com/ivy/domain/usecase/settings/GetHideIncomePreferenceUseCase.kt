package com.ivy.domain.usecase.settings

import com.ivy.data.api.BalancePrivacyPreferenceStore
import javax.inject.Inject

class GetHideIncomePreferenceUseCase @Inject internal constructor(
    private val balancePrivacyPreferenceStore: BalancePrivacyPreferenceStore,
) {
    operator fun invoke(): Boolean {
        return balancePrivacyPreferenceStore.hideIncome
    }
}
