package com.ivy.data.store

import com.ivy.data.api.LoanRecordStore
import com.ivy.data.db.dao.read.LoanRecordDao
import com.ivy.data.db.dao.write.WriteLoanRecordDao
import com.ivy.data.db.entity.LoanRecordEntity
import com.ivy.data.model.LoanRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class RoomLoanRecordStore @Inject constructor(
    private val loanRecordDao: LoanRecordDao,
    private val loanRecordWriter: WriteLoanRecordDao,
) : LoanRecordStore {
    override suspend fun findAll(): List<LoanRecord> = withContext(Dispatchers.IO) {
        loanRecordDao.findAll().map { it.toLegacyModel() }
    }

    override suspend fun findById(id: UUID): LoanRecord? = withContext(Dispatchers.IO) {
        loanRecordDao.findById(id)?.toLegacyModel()
    }

    override suspend fun findAllByLoanId(loanId: UUID): List<LoanRecord> =
        withContext(Dispatchers.IO) {
            loanRecordDao.findAllByLoanId(loanId).map { it.toLegacyModel() }
        }

    override suspend fun save(value: LoanRecord) {
        withContext(Dispatchers.IO) {
            loanRecordWriter.save(value.toEntity())
        }
    }

    override suspend fun saveMany(values: List<LoanRecord>) {
        withContext(Dispatchers.IO) {
            loanRecordWriter.saveMany(values.map { it.toEntity() })
        }
    }

    override suspend fun deleteById(id: UUID) {
        withContext(Dispatchers.IO) {
            loanRecordWriter.deleteById(id)
        }
    }

    override suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
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
