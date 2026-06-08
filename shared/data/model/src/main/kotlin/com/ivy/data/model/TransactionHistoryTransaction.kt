package com.ivy.data.model

import kotlinx.collections.immutable.ImmutableList

data class TransactionHistoryTransaction(
    val transaction: Transaction,
    val tags: ImmutableList<Tag>,
) : TransactionHistoryItem
