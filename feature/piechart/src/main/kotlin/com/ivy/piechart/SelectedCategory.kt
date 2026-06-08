package com.ivy.piechart

import androidx.compose.runtime.Immutable
import java.util.UUID

@Immutable
internal data class SelectedCategory(
    val categoryId: UUID
)
