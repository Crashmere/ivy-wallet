package com.ivy.domain.usecase.transaction

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.read.TransactionDao
import kotlinx.coroutines.withContext
import javax.inject.Inject

class HasTransactionsUseCase @Inject constructor(
    private val transactionDao: TransactionDao,
    private val dispatchers: DispatchersProvider
) {
    suspend operator fun invoke(): Boolean {
        return withContext(dispatchers.io) {
            transactionDao.hasAny()
        }
    }
}
