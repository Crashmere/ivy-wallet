package com.ivy.legacy.domain.mapper

import com.ivy.data.db.entity.CategoryEntity
import com.ivy.legacy.domain.model.Category

fun CategoryEntity.toLegacyDomain(): Category = Category(
    name = name,
    color = color,
    icon = icon,
    orderNum = orderNum,
    isDeleted = isDeleted,
    id = id
)
