package com.ivy.data.api

import com.ivy.data.model.Budget
import java.util.UUID

interface BudgetStore {
    suspend fun findAll(): List<Budget>

    suspend fun findById(id: UUID): Budget?

    suspend fun findMaxOrderNum(): Double

    suspend fun save(value: Budget)

    suspend fun saveMany(values: List<Budget>)

    suspend fun deleteById(id: UUID)

    suspend fun deleteAll()
}
