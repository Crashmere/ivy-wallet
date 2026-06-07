package com.ivy.data.api

import com.ivy.data.model.Account
import com.ivy.data.model.AccountId

interface AccountStore {
    suspend fun findById(id: AccountId): Account?

    suspend fun findAll(): List<Account>

    suspend fun findMaxOrderNum(): Double

    suspend fun save(value: Account)

    suspend fun saveMany(values: List<Account>)

    suspend fun deleteById(id: AccountId)

    suspend fun deleteAll()
}
