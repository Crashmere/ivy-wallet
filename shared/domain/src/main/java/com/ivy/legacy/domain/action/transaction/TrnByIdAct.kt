package com.ivy.legacy.domain.action.transaction

import com.ivy.base.model.legacy.Transaction
import com.ivy.data.model.TransactionId
import com.ivy.data.repository.TransactionRepository
import com.ivy.data.repository.mapper.TransactionMapper
import com.ivy.legacy.frp.action.FPAction
import com.ivy.legacy.frp.then
import com.ivy.legacy.domain.mapper.toLegacy
import java.util.UUID
import javax.inject.Inject

class TrnByIdAct @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val mapper: TransactionMapper
) : FPAction<UUID, Transaction?>() {
    override suspend fun UUID.compose(): suspend () -> Transaction? = suspend {
        this // transactionId
    } then {
        transactionRepo.findById(TransactionId(it))
    } then {
        it?.toLegacy(mapper)
    }
}
