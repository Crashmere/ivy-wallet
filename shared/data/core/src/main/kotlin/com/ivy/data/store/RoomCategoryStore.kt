package com.ivy.data.store

import com.ivy.data.api.DataWriteEvent
import com.ivy.data.api.CategoryStore
import com.ivy.data.db.dao.read.CategoryDao
import com.ivy.data.db.dao.write.WriteCategoryDao
import com.ivy.data.model.Category
import com.ivy.data.model.CategoryId
import com.ivy.data.mapper.CategoryMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomCategoryStore @Inject constructor(
    private val mapper: CategoryMapper,
    private val writeCategoryDao: WriteCategoryDao,
    private val categoryDao: CategoryDao,
    cacheFactory: StoreCacheFactory,
) : CategoryStore {
    private val cache = cacheFactory.createCache(
        getDataWriteSaveEvent = DataWriteEvent::SaveCategories,
        getDateWriteDeleteEvent = DataWriteEvent::DeleteCategories,
    )

    override suspend fun findAll(): List<Category> = cache.findAll(
        findAllOperation = {
            categoryDao.findAll().mapNotNull {
                with(mapper) { it.toDomain() }.getOrNull()
            }
        },
        sortCache = { sortedBy(Category::orderNum) }
    )

    override suspend fun findById(id: CategoryId): Category? = cache.findById(
        id = id,
        findByIdOperation = {
            categoryDao.findById(id.value)?.let {
                with(mapper) { it.toDomain() }.getOrNull()
            }
        }
    )

    override suspend fun findMaxOrderNum(): Double = if (cache.hasCachedAllItems) {
        cache.items.maxOfOrNull { (_, acc) -> acc.orderNum } ?: 0.0
    } else {
        withContext(Dispatchers.IO) {
            categoryDao.findMaxOrderNum() ?: 0.0
        }
    }

    override suspend fun save(value: Category): Unit = cache.save(
        value = value,
    ) {
        writeCategoryDao.save(
            with(mapper) { it.toEntity() }
        )
    }

    override suspend fun saveMany(values: List<Category>): Unit = cache.saveMany(
        values = values,
    ) {
        writeCategoryDao.saveMany(
            values.map { with(mapper) { it.toEntity() } }
        )
    }

    override suspend fun deleteById(id: CategoryId): Unit = cache.deleteById(id = id) {
        writeCategoryDao.deleteById(id.value)
    }

    override suspend fun deleteAll(): Unit = cache.deleteAll(writeCategoryDao::deleteAll)
}
