package com.ivy.legacy.domain.action.settings

import com.ivy.data.repository.CurrencyRepository
import com.ivy.frp.action.FPAction
import javax.inject.Inject

class BaseCurrencyAct @Inject constructor(
    private val currencyRepository: CurrencyRepository
) : FPAction<Unit, String>() {
    override suspend fun Unit.compose(): suspend () -> String = suspend {
        currencyRepository.getBaseCurrencyCode()
    }
}
