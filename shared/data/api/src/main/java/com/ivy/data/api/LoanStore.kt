package com.ivy.data.api

import com.ivy.data.model.legacy.Loan
import java.util.UUID

interface LoanStore {
    suspend fun findAll(): List<Loan>

    suspend fun findById(id: UUID): Loan?

    suspend fun findMaxOrderNum(): Double

    suspend fun save(value: Loan)

    suspend fun saveMany(values: List<Loan>)

    suspend fun deleteById(id: UUID)

    suspend fun deleteAll()
}
