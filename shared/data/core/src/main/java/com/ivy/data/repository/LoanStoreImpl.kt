package com.ivy.data.repository

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.api.LoanStore
import com.ivy.data.db.dao.read.LoanDao
import com.ivy.data.db.dao.write.WriteLoanDao
import com.ivy.data.db.entity.LoanEntity
import com.ivy.data.model.legacy.Loan
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class LoanStoreImpl @Inject constructor(
    private val loanDao: LoanDao,
    private val loanWriter: WriteLoanDao,
    private val dispatchers: DispatchersProvider
) : LoanStore {
    override suspend fun findAll(): List<Loan> = withContext(dispatchers.io) {
        loanDao.findAll().map { it.toLegacyModel() }
    }

    override suspend fun findById(id: UUID): Loan? = withContext(dispatchers.io) {
        loanDao.findById(id)?.toLegacyModel()
    }

    override suspend fun findMaxOrderNum(): Double = withContext(dispatchers.io) {
        loanDao.findMaxOrderNum() ?: 0.0
    }

    override suspend fun save(value: Loan) {
        withContext(dispatchers.io) {
            loanWriter.save(value.toEntity())
        }
    }

    override suspend fun saveMany(values: List<Loan>) {
        withContext(dispatchers.io) {
            loanWriter.saveMany(values.map { it.toEntity() })
        }
    }

    override suspend fun deleteById(id: UUID) {
        withContext(dispatchers.io) {
            loanWriter.deleteById(id)
        }
    }

    override suspend fun deleteAll() {
        withContext(dispatchers.io) {
            loanWriter.deleteAll()
        }
    }

    private fun LoanEntity.toLegacyModel(): Loan = Loan(
        name = name,
        amount = amount,
        type = type,
        color = color,
        icon = icon,
        orderNum = orderNum,
        accountId = accountId,
        note = note,
        isDeleted = isDeleted,
        id = id,
        dateTime = dateTime
    )

    private fun Loan.toEntity(): LoanEntity = LoanEntity(
        name = name,
        amount = amount,
        type = type,
        color = color,
        icon = icon,
        orderNum = orderNum,
        accountId = accountId,
        note = note,
        isDeleted = isDeleted,
        id = id,
        dateTime = dateTime
    )
}
