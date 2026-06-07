package com.ivy.data.api

import com.ivy.data.model.Category
import com.ivy.data.model.CategoryId

interface CategoryStore {
    suspend fun findAll(): List<Category>

    suspend fun findById(id: CategoryId): Category?

    suspend fun findMaxOrderNum(): Double

    suspend fun save(value: Category)

    suspend fun saveMany(values: List<Category>)

    suspend fun deleteById(id: CategoryId)

    suspend fun deleteAll()
}
