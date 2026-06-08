package com.ivy.data.store

import com.ivy.data.api.LoanStore
import com.ivy.data.db.dao.read.LoanDao
import com.ivy.data.db.dao.write.WriteLoanDao
import com.ivy.data.db.entity.LoanEntity
import com.ivy.data.model.Loan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

internal class RoomLoanStore @Inject constructor(
    private val loanDao: LoanDao,
    private val loanWriter: WriteLoanDao,
) : LoanStore {
    override suspend fun findAll(): List<Loan> = withContext(Dispatchers.IO) {
        loanDao.findAll().map { it.toLegacyModel() }
    }

    override suspend fun findById(id: UUID): Loan? = withContext(Dispatchers.IO) {
        loanDao.findById(id)?.toLegacyModel()
    }

    override suspend fun findMaxOrderNum(): Double = withContext(Dispatchers.IO) {
        loanDao.findMaxOrderNum() ?: 0.0
    }

    override suspend fun save(value: Loan) {
        withContext(Dispatchers.IO) {
            loanWriter.save(value.toEntity())
        }
    }

    override suspend fun saveMany(values: List<Loan>) {
        withContext(Dispatchers.IO) {
            loanWriter.saveMany(values.map { it.toEntity() })
        }
    }

    override suspend fun deleteById(id: UUID) {
        withContext(Dispatchers.IO) {
            loanWriter.deleteById(id)
        }
    }

    override suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
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
