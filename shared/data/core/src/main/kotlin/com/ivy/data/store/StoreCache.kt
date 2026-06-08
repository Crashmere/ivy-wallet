package com.ivy.data.store

import com.ivy.data.api.DataChangePublisher
import com.ivy.data.api.DataWriteEvent
import com.ivy.data.api.DeleteOperation
import com.ivy.data.model.identity.Identifiable
import com.ivy.data.model.identity.UniqueId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StoreCacheFactory @Inject constructor(
    private val dataChangePublisher: DataChangePublisher,
) {
    fun <T : Identifiable<TID>, TID : UniqueId> createCache(
        getDataWriteSaveEvent: ((List<T>) -> DataWriteEvent)? = null,
        getDataWriteDeleteEvent: ((DeleteOperation<TID>) -> DataWriteEvent)? = null,
    ): StoreCache<T, TID> = StoreCache(
        dataChangePublisher = dataChangePublisher,
        getDataWriteSaveEvent = getDataWriteSaveEvent,
        getDataWriteDeleteEvent = getDataWriteDeleteEvent,
    )
}

class StoreCache<T : Identifiable<TID>, TID : UniqueId> internal constructor(
    private val dataChangePublisher: DataChangePublisher,
    private val getDataWriteSaveEvent: ((List<T>) -> DataWriteEvent)?,
    private val getDataWriteDeleteEvent: ((DeleteOperation<TID>) -> DataWriteEvent)?,
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
            withContext(Dispatchers.IO) {
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
        return items[id] ?: withContext(Dispatchers.IO) {
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
        withContext(Dispatchers.IO) {
            writeOperation(value)
            cache(value)
            getDataWriteSaveEvent?.let { dataChangePublisher.post(it(listOf(value))) }
        }
    }

    suspend fun saveMany(
        values: List<T>,
        writeOperation: suspend (List<T>) -> Unit,
    ) {
        withContext(Dispatchers.IO) {
            writeOperation(values)
            cache(values)
            getDataWriteSaveEvent?.let { dataChangePublisher.post(it(values)) }
        }
    }

    suspend fun deleteById(
        id: TID,
        deleteByIdOperation: suspend (TID) -> Unit,
    ) {
        withContext(Dispatchers.IO) {
            cachedItems.remove(id)
            deleteByIdOperation(id)
            getDataWriteDeleteEvent?.let {
                dataChangePublisher.post(it(DeleteOperation.Just(listOf(id))))
            }
        }
    }

    suspend fun deleteAll(
        deleteAllOperation: suspend () -> Unit,
    ) {
        withContext(Dispatchers.IO) {
            cachedItems.clear()
            deleteAllOperation()
            getDataWriteDeleteEvent?.let {
                dataChangePublisher.post(it(DeleteOperation.All))
            }
        }
    }

    private fun cache(items: List<T>) {
        items.forEach(::cache)
    }

    private fun cache(item: T) {
        cachedItems[item.id] = item
    }
}
