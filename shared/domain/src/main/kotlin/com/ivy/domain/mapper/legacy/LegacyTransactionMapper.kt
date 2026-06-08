package com.ivy.domain.mapper.legacy

import com.ivy.data.model.TransactionType
import com.ivy.data.model.LegacyTag
import com.ivy.data.model.legacy.LegacyTransaction
import com.ivy.data.model.Expense
import com.ivy.data.model.Income
import com.ivy.data.model.Tag
import com.ivy.data.model.Transaction
import com.ivy.data.model.Transfer
import com.ivy.data.model.getFromAccount
import com.ivy.data.model.getFromValue
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

internal fun Transaction.toLegacyTransaction(
    tags: ImmutableList<LegacyTag> = persistentListOf()
): LegacyTransaction {
    val amount = getFromValue().amount.value.toBigDecimal()
    return LegacyTransaction(
        accountId = getFromAccount().value,
        type = when (this) {
            is Expense -> TransactionType.EXPENSE
            is Income -> TransactionType.INCOME
            is Transfer -> TransactionType.TRANSFER
        },
        amount = amount,
        toAccountId = if (this is Transfer) toAccount.value else null,
        toAmount = if (this is Transfer) toValue.amount.value.toBigDecimal() else amount,
        title = title?.value,
        description = description?.value,
        dateTime = time.takeIf { settled },
        categoryId = category?.value,
        dueDate = time.takeIf { !settled },
        recurringRuleId = metadata.recurringRuleId,
        paidFor = metadata.paidForDateTime,
        attachmentUrl = null,
        loanId = metadata.loanId,
        loanRecordId = metadata.loanRecordId,
        id = id.value,
        tags = tags
    )
}

internal fun Tag.toLegacyTag(): LegacyTag = LegacyTag(this.id.value, this.name.value)
internal fun List<Tag>.toImmutableLegacyTags(): ImmutableList<LegacyTag> =
    this.map { it.toLegacyTag() }.toImmutableList()
