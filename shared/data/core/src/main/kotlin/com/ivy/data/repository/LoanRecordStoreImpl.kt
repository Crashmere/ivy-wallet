package com.ivy.data.repository

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.api.LoanRecordStore
import com.ivy.data.db.dao.read.LoanRecordDao
import com.ivy.data.db.dao.write.WriteLoanRecordDao
import com.ivy.data.db.entity.LoanRecordEntity
import com.ivy.data.model.legacy.LoanRecord
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class LoanRecordStoreImpl @Inject constructor(
    private val loanRecordDao: LoanRecordDao,
    private val loanRecordWriter: WriteLoanRecordDao,
    private val dispatchers: DispatchersProvider
) : LoanRecordStore {
    override suspend fun findAll(): List<LoanRecord> = withContext(dispatchers.io) {
        loanRecordDao.findAll().map { it.toLegacyModel() }
    }

    override suspend fun findById(id: UUID): LoanRecord? = withContext(dispatchers.io) {
        loanRecordDao.findById(id)?.toLegacyModel()
    }

    override suspend fun findAllByLoanId(loanId: UUID): List<LoanRecord> =
        withContext(dispatchers.io) {
            loanRecordDao.findAllByLoanId(loanId).map { it.toLegacyModel() }
        }

    override suspend fun save(value: LoanRecord) {
        withContext(dispatchers.io) {
            loanRecordWriter.save(value.toEntity())
        }
    }

    override suspend fun saveMany(values: List<LoanRecord>) {
        withContext(dispatchers.io) {
            loanRecordWriter.saveMany(values.map { it.toEntity() })
        }
    }

    override suspend fun deleteById(id: UUID) {
        withContext(dispatchers.io) {
            loanRecordWriter.deleteById(id)
        }
    }

    override suspend fun deleteAll() {
        withContext(dispatchers.io) {
            loanRecordWriter.deleteAll()
        }
    }

    private fun LoanRecordEntity.toLegacyModel(): LoanRecord = LoanRecord(
        loanId = loanId,
        amount = amount,
        note = note,
        dateTime = dateTime,
        interest = interest,
        accountId = accountId,
        convertedAmount = convertedAmount,
        loanRecordType = loanRecordType,
        isDeleted = isDeleted,
        id = id
    )

    private fun LoanRecord.toEntity(): LoanRecordEntity = LoanRecordEntity(
        loanId = loanId,
        amount = amount,
        note = note,
        dateTime = dateTime,
        interest = interest,
        accountId = accountId,
        convertedAmount = convertedAmount,
        loanRecordType = loanRecordType,
        isDeleted = isDeleted,
        id = id
    )
}
