package com.ivy.data.api

import com.ivy.data.model.LoanRecord
import java.util.UUID

interface LoanRecordStore {
    suspend fun findAll(): List<LoanRecord>

    suspend fun findById(id: UUID): LoanRecord?

    suspend fun findAllByLoanId(loanId: UUID): List<LoanRecord>

    suspend fun save(value: LoanRecord)

    suspend fun saveMany(values: List<LoanRecord>)

    suspend fun deleteById(id: UUID)

    suspend fun deleteAll()
}
