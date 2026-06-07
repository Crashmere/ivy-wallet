package com.ivy.legacy.domain.model

data class CreateAccountData(
    val name: String,
    val currency: String,
    val color: Int,
    val icon: String?,
    val balance: Double,
    val includeBalance: Boolean = true,
)
