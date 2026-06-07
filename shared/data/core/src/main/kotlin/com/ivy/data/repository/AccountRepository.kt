package com.ivy.data.repository

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.api.DataWriteEvent
import com.ivy.data.api.AccountStore
import com.ivy.data.db.dao.read.AccountDao
import com.ivy.data.db.dao.write.WriteAccountDao
import com.ivy.data.model.Account
import com.ivy.data.model.AccountId
import com.ivy.data.repository.mapper.AccountMapper
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val mapper: AccountMapper,
    private val accountDao: AccountDao,
    private val writeAccountDao: WriteAccountDao,
    private val dispatchersProvider: DispatchersProvider,
    cacheFactory: RepositoryCacheFactory,
) : AccountStore {
    private val cache = cacheFactory.createCache(
        getDataWriteSaveEvent = DataWriteEvent::SaveAccounts,
        getDateWriteDeleteEvent = DataWriteEvent::DeleteAccounts
    )

    override suspend fun findById(id: AccountId): Account? = cache.findById(
        id = id,
        findByIdOperation = {
            accountDao.findById(id.value)?.let {
                with(mapper) { it.toDomain() }.getOrNull()
            }
        }
    )

    override suspend fun findAll(): List<Account> = cache.findAll(
        findAllOperation = {
            accountDao.findAll().mapNotNull {
                with(mapper) { it.toDomain() }.getOrNull()
            }
        },
        sortCache = { sortedBy(Account::orderNum) }
    )

    override suspend fun findMaxOrderNum(): Double = if (cache.hasCachedAllItems) {
        cache.items.maxOfOrNull { (_, acc) -> acc.orderNum } ?: 0.0
    } else {
        withContext(dispatchersProvider.io) {
            accountDao.findMaxOrderNum() ?: 0.0
        }
    }

    override suspend fun save(value: Account): Unit = cache.save(value) {
        writeAccountDao.save(
            with(mapper) { it.toEntity() }
        )
    }

    override suspend fun saveMany(values: List<Account>): Unit = cache.saveMany(values) {
        writeAccountDao.saveMany(
            it.map { with(mapper) { it.toEntity() } }
        )
    }

    override suspend fun deleteById(id: AccountId): Unit = cache.deleteById(id) {
        writeAccountDao.deleteById(id.value)
    }

    override suspend fun deleteAll(): Unit = cache.deleteAll(
        deleteAllOperation = writeAccountDao::deleteAll
    )
}
