package com.ivy.data.repository

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.api.DataChangePublisher
import com.ivy.data.api.DataWriteEvent
import com.ivy.data.api.DeleteOperation
import com.ivy.data.model.identity.Identifiable
import com.ivy.data.model.identity.UniqueId
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RepositoryCacheFactory @Inject constructor(
    private val dataChangePublisher: DataChangePublisher,
    private val dispatchers: DispatchersProvider,
) {
    fun <T : Identifiable<TID>, TID : UniqueId> createCache(
        getDataWriteSaveEvent: (List<T>) -> DataWriteEvent,
        getDateWriteDeleteEvent: (DeleteOperation<TID>) -> DataWriteEvent
    ): RepositoryCache<T, TID> = RepositoryCache(
        dataChangePublisher = dataChangePublisher,
        dispatchers = dispatchers,
        getDataWriteSaveEvent = getDataWriteSaveEvent,
        getDataWriteDeleteEvent = getDateWriteDeleteEvent,
    )
}

class RepositoryCache<T : Identifiable<TID>, TID : UniqueId> internal constructor(
    private val dataChangePublisher: DataChangePublisher,
    private val dispatchers: DispatchersProvider,
    private val getDataWriteSaveEvent: (List<T>) -> DataWriteEvent,
    private val getDataWriteDeleteEvent: (DeleteOperation<TID>) -> DataWriteEvent,
) {

    private val cachedItems = mutableMapOf<TID, T>()
    val items: Map<TID, T> = cachedItems
    var hasCachedAllItems = false
        private set

    suspend fun findAll(
        findAllOperation: suspend () -> List<T>,
        sortCache: Collection<T>.() -> List<T>,
    ): List<T> {
        return if (hasCachedAllItems) {
            sortCache(cachedItems.values)
        } else {
            withContext(dispatchers.io) {
                findAllOperation().also {
                    cache(it)
                    hasCachedAllItems = true
                }
            }
        }
    }

    suspend fun findById(
        id: TID,
        findByIdOperation: suspend (TID) -> T?
    ): T? {
        return items[id] ?: withContext(dispatchers.io) {
            findByIdOperation(id)?.also(::cache)
        }
    }

    suspend fun findByIds(
        ids: List<TID>,
        findByIdOperation: suspend (TID) -> T?
    ): List<T> = ids.mapNotNull { id -> findById(id, findByIdOperation) }

    suspend fun save(
        value: T,
        writeOperation: suspend (T) -> Unit,
    ) {
        withContext(dispatchers.io) {
            writeOperation(value)
            cache(value)
            dataChangePublisher.post(getDataWriteSaveEvent(listOf(value)))
        }
    }

    suspend fun saveMany(
        values: List<T>,
        writeOperation: suspend (List<T>) -> Unit,
    ) {
        withContext(dispatchers.io) {
            writeOperation(values)
            cache(values)
            dataChangePublisher.post(getDataWriteSaveEvent(values))
        }
    }

    suspend fun deleteById(
        id: TID,
        deleteByIdOperation: suspend (TID) -> Unit,
    ) {
        withContext(dispatchers.io) {
            cachedItems.remove(id)
            deleteByIdOperation(id)
            dataChangePublisher.post(
                getDataWriteDeleteEvent(DeleteOperation.Just(listOf(id)))
            )
        }
    }

    suspend fun deleteAll(
        deleteAllOperation: suspend () -> Unit,
    ) {
        withContext(dispatchers.io) {
            cachedItems.clear()
            deleteAllOperation()
            dataChangePublisher.post(getDataWriteDeleteEvent(DeleteOperation.All))
        }
    }

    private fun cache(items: List<T>) {
        items.forEach(::cache)
    }

    private fun cache(item: T) {
        cachedItems[item.id] = item
    }
}
