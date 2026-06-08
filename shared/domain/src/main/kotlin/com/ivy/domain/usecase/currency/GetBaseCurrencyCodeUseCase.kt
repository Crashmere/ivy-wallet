package com.ivy.domain.usecase.currency

import javax.inject.Inject

class GetBaseCurrencyCodeUseCase @Inject internal constructor(
    private val getBaseCurrency: GetBaseCurrencyUseCase
) {
    suspend operator fun invoke(): String {
        return getBaseCurrency().code
    }
}
