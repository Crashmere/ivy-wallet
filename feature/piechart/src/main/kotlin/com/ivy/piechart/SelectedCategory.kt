package com.ivy.piechart

import androidx.compose.runtime.Immutable
import com.ivy.data.model.Category

@Immutable
internal data class SelectedCategory(
    val category: Category // null - Unspecified
)
