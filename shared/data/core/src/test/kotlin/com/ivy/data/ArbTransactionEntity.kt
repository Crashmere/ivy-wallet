package com.ivy.data

import com.ivy.data.model.TransactionType
import com.ivy.data.db.entity.TransactionEntity
import com.ivy.testing.accountId
import com.ivy.testing.maybe
import com.ivy.testing.or
import com.ivy.testing.positiveDoubleExact
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.instant
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.negativeDouble
import io.kotest.property.arbitrary.of
import io.kotest.property.arbitrary.string
import io.kotest.property.arbitrary.uuid
import java.util.UUID

internal fun Arb.Companion.invalidTransactionEntity(): Arb<TransactionEntity> = Arb.or(
    a = Arb.invalidIncomeOrExpense(),
    b = Arb.invalidTransfer()
)

internal fun Arb.Companion.validTransactionEntity(): Arb<TransactionEntity> = Arb.or(
    a = Arb.validIncomeOrExpense(),
    b = Arb.validTransfer()
)

internal fun Arb.Companion.invalidTransfer(): Arb<TransactionEntity> = arbitrary {
    var entity = validTransfer().bind()
    val invalidReasons = InvalidTransferReason.entries.shuffled().take(
        Arb.int(1 until InvalidTransferReason.entries.size).bind()
    ).toSet()

    if (InvalidTransferReason.SameAccountAndToAccount in invalidReasons) {
        val accountId = UUID.randomUUID()
        entity = entity.copy(
            accountId = accountId,
            toAccountId = accountId
        )
    }

    if (InvalidTransferReason.MissingToAccount in invalidReasons) {
        entity = entity.copy(
            toAccountId = null
        )
    }

    entity
}

internal fun Arb.Companion.validTransfer(): Arb<TransactionEntity> = arbitrary {
    val isPlannedPayment = Arb.boolean().bind()

    val account = Arb.accountId().bind().value
    val toAccount = Arb.accountId()
        .filter { it.value != account }
        .bind().value

    TransactionEntity(
        accountId = account,
        type = TransactionType.TRANSFER,
        amount = Arb.positiveDoubleExact().bind().value,
        toAccountId = toAccount,
        toAmount = Arb.maybe(Arb.positiveDoubleExact()).bind()?.value,
        title = Arb.maybe(Arb.string()).bind(),
        description = Arb.maybe(Arb.string()).bind(),
        dateTime = Arb.instant().bind().takeIf {
            !isPlannedPayment || Arb.boolean().bind()
        },
        dueDate = Arb.instant().bind().takeIf {
            isPlannedPayment || Arb.boolean().bind()
        },
        paidForDateTime = Arb.instant().bind().takeIf {
            !isPlannedPayment || Arb.boolean().bind()
        },
        categoryId = Arb.maybe(Arb.uuid()).bind(),
        recurringRuleId = Arb.maybe(Arb.uuid()).bind(),
        attachmentUrl = Arb.maybe(Arb.string()).bind(),
        loanId = Arb.maybe(Arb.uuid()).bind(),
        loanRecordId = Arb.maybe(Arb.uuid()).bind(),
        isDeleted = false,
        id = Arb.uuid().bind()
    )
}

internal fun Arb.Companion.invalidIncomeOrExpense(): Arb<TransactionEntity> = arbitrary {
    var entity = validIncomeOrExpense().bind()
    val invalidReasons = InvalidIncomeOrExpenseReason.entries.shuffled().take(
        Arb.int(1 until InvalidIncomeOrExpenseReason.entries.size).bind()
    ).toSet()

    if (InvalidIncomeOrExpenseReason.MissingTime in invalidReasons) {
        entity = entity.copy(
            dateTime = null,
            dueDate = null,
        )
    }
    if (InvalidIncomeOrExpenseReason.InfiniteAmount in invalidReasons) {
        entity = entity.copy(
            amount = Double.POSITIVE_INFINITY
        )
    }
    if (InvalidIncomeOrExpenseReason.NonPositiveAmount in invalidReasons) {
        entity = entity.copy(
            amount = Arb.or(Arb.negativeDouble(), Arb.of(0.0)).bind()
        )
    }

    entity
}

internal fun Arb.Companion.validIncomeOrExpense(): Arb<TransactionEntity> = arbitrary {
    val isPlannedPayment = Arb.boolean().bind()

    TransactionEntity(
        accountId = Arb.uuid().bind(),
        type = Arb.of(TransactionType.INCOME, TransactionType.EXPENSE).bind(),
        amount = Arb.positiveDoubleExact().bind().value,
        toAccountId = Arb.maybe(Arb.accountId()).bind()?.value,
        toAmount = Arb.maybe(Arb.double()).bind(),
        title = Arb.maybe(Arb.string()).bind(),
        description = Arb.maybe(Arb.string()).bind(),
        dateTime = Arb.instant().bind().takeIf {
            !isPlannedPayment || Arb.boolean().bind()
        },
        dueDate = Arb.instant().bind().takeIf {
            isPlannedPayment || Arb.boolean().bind()
        },
        paidForDateTime = Arb.instant().bind().takeIf {
            !isPlannedPayment || Arb.boolean().bind()
        },
        categoryId = Arb.maybe(Arb.uuid()).bind(),
        recurringRuleId = Arb.maybe(Arb.uuid()).bind(),
        attachmentUrl = Arb.maybe(Arb.string()).bind(),
        loanId = Arb.maybe(Arb.uuid()).bind(),
        loanRecordId = Arb.maybe(Arb.uuid()).bind(),
        isDeleted = false,
        id = Arb.uuid().bind()
    )
}

internal enum class InvalidIncomeOrExpenseReason {
    MissingTime,
    NonPositiveAmount,
    InfiniteAmount,
}

internal enum class InvalidTransferReason {
    MissingToAccount,
    SameAccountAndToAccount,
}
