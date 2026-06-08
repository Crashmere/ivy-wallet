package com.ivy.ui.modal

import java.util.UUID

data class AccountModalAccount(
    val id: UUID,
    val name: String,
    val color: Int,
    val currency: String?,
    val icon: String?,
    val includeInBalance: Boolean,
)
