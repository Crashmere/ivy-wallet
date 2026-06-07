package com.ivy.data.model

import com.ivy.data.model.primitive.AssetCode
import com.ivy.data.model.primitive.PositiveDouble
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.Test
import java.time.Instant
import java.util.UUID

class TransactionTest {
    @Test
    fun `getFromAccount - income`() {
        // given
        val transaction = Income

        // when
        val accountId = transaction.getFromAccount()

        // then
        accountId shouldBe AccountId
    }

    @Test
    fun `getFromAccount - expense`() {
        // given
        val transaction = Expense

        // when
        val accountId = transaction.getFromAccount()

        // then
        accountId.shouldNotBeNull() shouldBe AccountId
    }

    @Test
    fun `getFromAccount - transfer`() {
        // given
        val transaction = Transfer

        // when
        val accountId = transaction.getFromAccount()

        // then
        accountId shouldBe AccountId
    }

    @Test
    fun `getToAccount - income`() {
        // given
        val transaction = Income

        // when
        val accountId = transaction.getToAccount()

        // then
        accountId shouldBe null
    }

    @Test
    fun `getToAccount - expense`() {
        // given
        val transaction = Expense

        // when
        val accountId = transaction.getToAccount()

        // then
        accountId shouldBe null
    }

    @Test
    fun `getToAccount - transfer`() {
        // given
        val transaction = Transfer

        // when
        val accountId = transaction.getToAccount()

        // then
        accountId shouldBe ToAccountId
    }

    @Test
    fun `getFromValue - income`() {
        // given
        val transaction = Income

        // when
        val value = transaction.getFromValue()

        // then
        value shouldBe Income.value
    }

    @Test
    fun `getFromValue - expense`() {
        // given
        val transaction = Expense

        // when
        val value = transaction.getFromValue()

        // then
        value shouldBe Expense.value
    }

    @Test
    fun `getFromValue - transfer`() {
        // given
        val transaction = Transfer

        // when
        val value = transaction.getFromValue()

        // then
        value shouldBe Transfer.fromValue
    }

    companion object {
        val AccountId = AccountId(UUID.randomUUID())
        val ToAccountId = AccountId(UUID.randomUUID())

        val Expense = Expense(
            id = TransactionId(UUID.randomUUID()),
            title = null,
            description = null,
            category = null,
            time = Instant.EPOCH,
            settled = false,
            metadata = TransactionMetadata(
                recurringRuleId = null,
                loanId = null,
                paidForDateTime = null,
                loanRecordId = null
            ),
            tags = listOf(),
            value = PositiveValue(
                amount = PositiveDouble.unsafe(1.0),
                asset = AssetCode.EUR
            ),
            account = AccountId,
        )

        val Income = Income(
            id = TransactionId(UUID.randomUUID()),
            title = null,
            description = null,
            category = null,
            time = Instant.EPOCH,
            settled = false,
            metadata = TransactionMetadata(
                recurringRuleId = null,
                loanId = null,
                paidForDateTime = null,
                loanRecordId = null
            ),
            tags = listOf(),
            value = PositiveValue(
                amount = PositiveDouble.unsafe(1.0),
                asset = AssetCode.EUR
            ),
            account = AccountId,
        )

        val Transfer = Transfer(
            id = TransactionId(UUID.randomUUID()),
            title = null,
            description = null,
            category = null,
            time = Instant.EPOCH,
            settled = false,
            metadata = TransactionMetadata(
                recurringRuleId = null,
                loanId = null,
                paidForDateTime = null,
                loanRecordId = null
            ),
            tags = listOf(),
            fromAccount = AccountId,
            fromValue = PositiveValue(
                amount = PositiveDouble.unsafe(1.0),
                asset = AssetCode.EUR
            ),
            toValue = PositiveValue(
                amount = PositiveDouble.unsafe(1.0),
                asset = AssetCode.EUR
            ),
            toAccount = ToAccountId,
        )
    }
}
