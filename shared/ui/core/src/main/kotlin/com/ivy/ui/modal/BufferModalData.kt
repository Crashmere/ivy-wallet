package com.ivy.ui.modal

import java.util.UUID

data class BufferModalData(
    val balance: Double,
    val buffer: Double,
    val currency: String,
    val id: UUID = UUID.randomUUID()
)
