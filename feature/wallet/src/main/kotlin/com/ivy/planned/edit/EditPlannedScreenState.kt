package com.ivy.planned.edit

import com.ivy.data.model.TransactionType
import com.ivy.data.model.Category
import com.ivy.data.model.IntervalType
import kotlinx.collections.immutable.ImmutableList
import java.time.LocalDateTime
import javax.annotation.concurrent.Immutable

@Immutable
internal data class EditPlannedScreenState(
    val currency: String,
    val transactionType: TransactionType,
    val startDate: LocalDateTime?,
    val intervalN: Int?,
    val intervalType: IntervalType?,
    val oneTime: Boolean,
    val initialTitle: String?,
    val description: String?,
    val categories: ImmutableList<Category>,
    val accounts: ImmutableList<EditPlannedAccount>,
    val account: EditPlannedAccount?,
    val category: Category?,
    val amount: Double,
    val categoryModalVisible: Boolean,
    val descriptionModalVisible: Boolean,
    val deleteTransactionModalVisible: Boolean,
    val transactionTypeModalVisible: Boolean,
    val amountModalVisible: Boolean
)
