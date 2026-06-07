package com.ivy.legacy.domain.model

import com.ivy.data.db.entity.CategoryEntity
import java.util.UUID

data class Category(
    val name: String,
    val color: Int,
    val icon: String? = null,
    val orderNum: Double = 0.0,

    val isDeleted: Boolean = false,

    val id: UUID = UUID.randomUUID()
) {
    fun toEntity(): CategoryEntity = CategoryEntity(
        name = name,
        color = color,
        icon = icon,
        orderNum = orderNum,
        isDeleted = isDeleted,
        id = id
    )
}
