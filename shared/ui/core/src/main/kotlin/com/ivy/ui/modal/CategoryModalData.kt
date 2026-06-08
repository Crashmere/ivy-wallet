package com.ivy.ui.modal

import com.ivy.data.model.Category
import java.util.UUID

data class CategoryModalData(
    val category: Category?,
    val id: UUID = UUID.randomUUID(),
    val autoFocusKeyboard: Boolean = true,
)
