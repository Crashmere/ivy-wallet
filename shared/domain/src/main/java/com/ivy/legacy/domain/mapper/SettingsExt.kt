package com.ivy.legacy.domain.mapper

import com.ivy.data.db.entity.SettingsEntity
import com.ivy.legacy.domain.model.Settings

fun SettingsEntity.toLegacyDomain(): Settings = Settings(
    theme = theme,
    baseCurrency = currency,
    bufferAmount = bufferAmount.toBigDecimal(),
    name = name,
    id = id
)
