package com.ivy.domain.mapper.legacy

import com.ivy.base.model.TransactionType
import com.ivy.base.model.legacy.LegacyTag
import com.ivy.base.model.legacy.LegacyTransaction
import com.ivy.data.api.AccountStore
import com.ivy.data.model.AccountId
import com.ivy.data.model.CategoryId
import com.ivy.data.model.Expense
import com.ivy.data.model.Income
import com.ivy.data.model.PositiveValue
import com.ivy.data.model.Tag
import com.ivy.data.model.TagId
import com.ivy.data.model.Transaction
import com.ivy.data.model.TransactionId
import com.ivy.data.model.TransactionMetadata
import com.ivy.data.model.Transfer
import com.ivy.data.model.getFromAccount
import com.ivy.data.model.getFromValue
import com.ivy.data.model.primitive.NotBlankTrimmedString
import com.ivy.data.model.primitive.PositiveDouble
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

fun Transaction.toLegacy(
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

suspend fun LegacyTransaction.toDomain(accountStore: AccountStore): Transaction? {
    val time = dateTime ?: dueDate ?: return null
    val sourceAccountId = AccountId(accountId)
    val sourceAccount = accountStore.findById(sourceAccountId) ?: return null
    val amount = PositiveDouble.from(amount.toDouble()).getOrNull() ?: return null
    val value = PositiveValue(
        amount = amount,
        asset = sourceAccount.asset
    )
    val transactionId = TransactionId(id)
    val notBlankTitle = title?.let(NotBlankTrimmedString::from)?.getOrNull()
    val notBlankDescription = description?.let(NotBlankTrimmedString::from)?.getOrNull()
    val category = categoryId?.let(::CategoryId)
    val settled = dateTime != null
    val metadata = TransactionMetadata(
        recurringRuleId = recurringRuleId,
        paidForDateTime = paidFor,
        loanId = loanId,
        loanRecordId = loanRecordId
    )
    val tagIds = tags.map { TagId(it.id) }

    return when (type) {
        TransactionType.INCOME -> Income(
            id = transactionId,
            value = value,
            account = sourceAccountId,
            title = notBlankTitle,
            description = notBlankDescription,
            category = category,
            time = time,
            settled = settled,
            metadata = metadata,
            tags = tagIds,
        )

        TransactionType.EXPENSE -> Expense(
            id = transactionId,
            account = sourceAccountId,
            value = value,
            title = notBlankTitle,
            description = notBlankDescription,
            category = category,
            time = time,
            settled = settled,
            metadata = metadata,
            tags = tagIds,
        )

        TransactionType.TRANSFER -> {
            val targetAccountId = toAccountId?.let(::AccountId) ?: return null
            if (sourceAccountId == targetAccountId) return null
            val targetAccount = accountStore.findById(targetAccountId) ?: return null
            val targetAmount = PositiveDouble.from(toAmount.toDouble()).getOrNull() ?: amount
            Transfer(
                id = transactionId,
                title = notBlankTitle,
                description = notBlankDescription,
                category = category,
                time = time,
                settled = settled,
                metadata = metadata,
                fromAccount = sourceAccountId,
                fromValue = value,
                toAccount = targetAccountId,
                toValue = PositiveValue(
                    amount = targetAmount,
                    asset = targetAccount.asset
                ),
                tags = tagIds,
            )
        }
    }
}

fun Tag.toLegacyTag(): LegacyTag = LegacyTag(this.id.value, this.name.value)
fun List<Tag>.toImmutableLegacyTags(): ImmutableList<LegacyTag> =
    this.map { it.toLegacyTag() }.toImmutableList()
