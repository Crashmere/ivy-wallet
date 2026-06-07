package com.ivy.data.store

import arrow.core.Either
import arrow.core.Some
import arrow.core.identity
import com.ivy.data.db.dao.fake.FakeTransactionDao
import com.ivy.data.db.dao.read.TransactionDao
import com.ivy.data.db.dao.write.WriteTransactionDao
import com.ivy.data.db.entity.TransactionEntity
import com.ivy.data.invalidTransactionEntity
import com.ivy.data.model.AccountId
import com.ivy.data.model.Expense
import com.ivy.data.model.Income
import com.ivy.data.model.Transaction
import com.ivy.data.model.TransactionType
import com.ivy.data.model.Transfer
import com.ivy.data.model.testing.ModelFixtures
import com.ivy.data.model.testing.accountId
import com.ivy.data.model.testing.transaction
import com.ivy.data.model.testing.transactionId
import com.ivy.data.api.TagStore
import com.ivy.data.mapper.TransactionMapper
import com.ivy.data.validTransactionEntity
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.instant
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class RoomTransactionStoreTest {

    private val mapper = mockk<TransactionMapper>()
    private val transactionDao = mockk<TransactionDao>()
    private val writeTransactionDao = mockk<WriteTransactionDao>()
    private val tagStore = mockk<TagStore>(relaxed = true)

    private lateinit var store: RoomTransactionStore

    @Before
    fun setup() {
        store = newStore(fakeDao = null)
    }

    private fun newStore(
        fakeDao: FakeTransactionDao?,
    ): RoomTransactionStore = RoomTransactionStore(
        mapper = mapper,
        transactionDao = fakeDao ?: transactionDao,
        writeTransactionDao = fakeDao ?: writeTransactionDao,
        tagStore = tagStore
    )

    @Test
    fun `find by id - not existing`() = runTest {
        // given
        val transactionId = ModelFixtures.TransactionId
        coEvery {
            transactionDao.findById(transactionId.value)
        } returns null

        // when
        val transaction = store.findById(transactionId)

        // then
        transaction shouldBe null
    }

    @Test
    fun `find by id - existing, successful mapping`() = runTest {
        // given
        val transactionId = ModelFixtures.TransactionId
        val entity = mockk<TransactionEntity>()
        val expectedTransaction = mockk<Transaction>()
        coEvery {
            transactionDao.findById(transactionId.value)
        } returns entity
        with(mapper) {
            coEvery { entity.toDomain(any()) } returns Either.Right(expectedTransaction)
        }

        // when
        val transaction = store.findById(transactionId)

        // then
        transaction shouldBe expectedTransaction
    }

    @Test
    fun `find by id - existing, failed mapping`() = runTest {
        // given
        val transactionId = ModelFixtures.TransactionId
        val entity = mockk<TransactionEntity>()
        coEvery {
            transactionDao.findById(transactionId.value)
        } returns entity
        with(mapper) {
            coEvery { entity.toDomain(any()) } returns Either.Left("err")
        }

        // when
        val transaction = store.findById(transactionId)

        // then
        transaction shouldBe null
    }

    @Test
    fun `find all`() = transactionsTestCase(
        daoMethod = transactionDao::findAll,
        repoMethod = store::findAll
    )

    @Test
    fun findAllIncomeByAccount() {
        val account = ModelFixtures.AccountId

        transactionsTestCase(
            daoMethod = {
                transactionDao.findAllByTypeAndAccount(
                    type = TransactionType.INCOME,
                    accountId = account.value
                )
            },
            repoMethod = {
                store.findAllIncomeByAccount(account)
            },
            mapExpectedResult = { it.filterIsInstance<Income>() }
        )
    }

    @Test
    fun findAllExpenseByAccount() {
        val account = ModelFixtures.AccountId

        transactionsTestCase(
            daoMethod = {
                transactionDao.findAllByTypeAndAccount(
                    type = TransactionType.EXPENSE,
                    accountId = account.value
                )
            },
            repoMethod = {
                store.findAllExpenseByAccount(account)
            },
            mapExpectedResult = { it.filterIsInstance<Expense>() }
        )
    }

    @Test
    fun findAllTransferByAccount() {
        val account = ModelFixtures.AccountId

        transactionsTestCase(
            daoMethod = {
                transactionDao.findAllByTypeAndAccount(
                    type = TransactionType.TRANSFER,
                    accountId = account.value
                )
            },
            repoMethod = {
                store.findAllTransferByAccount(account)
            },
            mapExpectedResult = { it.filterIsInstance<Transfer>() }
        )
    }

    @Test
    fun findAllTransfersToAccount() {
        val account = ModelFixtures.AccountId

        transactionsTestCase(
            daoMethod = {
                transactionDao.findAllTransfersToAccount(
                    toAccountId = account.value
                )
            },
            repoMethod = {
                store.findAllTransfersToAccount(account)
            },
            mapExpectedResult = { it.filterIsInstance<Transfer>() }
        )
    }

    @Test
    fun `find all by ids`() {
        val ids = Arb.list(Arb.transactionId()).next()
        transactionsTestCase(
            daoMethod = {
                transactionDao.findByIds(ids.map { it.value })
            },
            repoMethod = {
                store.findByIds(ids)
            }
        )
    }

    @Test
    fun `find all between`() {
        val startDate = Arb.instant().next()
        val endDate = Arb.instant().next()

        transactionsTestCase(
            daoMethod = {
                transactionDao.findAllBetween(
                    startDate = startDate,
                    endDate = endDate,
                )
            },
            repoMethod = {
                store.findAllBetween(
                    startDate = startDate,
                    endDate = endDate,
                )
            }
        )
    }

    @Test
    fun findAllByAccountAndBetween() {
        val account = ModelFixtures.AccountId
        val startDate = Arb.instant().next()
        val endDate = Arb.instant().next()

        transactionsTestCase(
            daoMethod = {
                transactionDao.findAllByAccountAndBetween(
                    accountId = account.value,
                    startDate = startDate,
                    endDate = endDate,
                )
            },
            repoMethod = {
                store.findAllByAccountAndBetween(
                    accountId = account,
                    startDate = startDate,
                    endDate = endDate,
                )
            }
        )
    }

    @Test
    fun findAllToAccountAndBetween() {
        val account = ModelFixtures.AccountId
        val startDate = Arb.instant().next()
        val endDate = Arb.instant().next()

        transactionsTestCase(
            daoMethod = {
                transactionDao.findAllToAccountAndBetween(
                    toAccountId = account.value,
                    startDate = startDate,
                    endDate = endDate,
                )
            },
            repoMethod = {
                store.findAllToAccountAndBetween(
                    toAccountId = account,
                    startDate = startDate,
                    endDate = endDate,
                )
            }
        )
    }

    @Test
    fun findAllDueToBetweenByCategory() {
        val category = ModelFixtures.CategoryId
        val startDate = Arb.instant().next()
        val endDate = Arb.instant().next()

        transactionsTestCase(
            daoMethod = {
                transactionDao.findAllDueToBetweenByCategory(
                    categoryId = category.value,
                    startDate = startDate,
                    endDate = endDate,
                )
            },
            repoMethod = {
                store.findAllDueToBetweenByCategory(
                    categoryId = category,
                    startDate = startDate,
                    endDate = endDate,
                )
            }
        )
    }

    @Test
    fun findAllDueToBetweenByCategoryUnspecified() {
        val startDate = Arb.instant().next()
        val endDate = Arb.instant().next()

        transactionsTestCase(
            daoMethod = {
                transactionDao.findAllDueToBetweenByCategoryUnspecified(
                    startDate = startDate,
                    endDate = endDate,
                )
            },
            repoMethod = {
                store.findAllDueToBetweenByCategoryUnspecified(
                    startDate = startDate,
                    endDate = endDate,
                )
            }
        )
    }

    @Test
    fun findAllDueToBetweenByAccount() {
        val account = ModelFixtures.AccountId
        val startDate = Arb.instant().next()
        val endDate = Arb.instant().next()

        transactionsTestCase(
            daoMethod = {
                transactionDao.findAllDueToBetweenByAccount(
                    accountId = account.value,
                    startDate = startDate,
                    endDate = endDate,
                )
            },
            repoMethod = {
                store.findAllDueToBetweenByAccount(
                    accountId = account,
                    startDate = startDate,
                    endDate = endDate,
                )
            }
        )
    }

    @Test
    fun findAllByCategoryAndTypeAndBetween() {
        val categoryId = ModelFixtures.CategoryId
        val transactionType = TransactionType.EXPENSE
        val startDate = Arb.instant().next()
        val endDate = Arb.instant().next()

        transactionsTestCase(
            daoMethod = {
                transactionDao.findAllByCategoryAndTypeAndBetween(
                    categoryId = categoryId.value,
                    type = transactionType,
                    startDate = startDate,
                    endDate = endDate,
                )
            },
            repoMethod = {
                store.findAllByCategoryAndTypeAndBetween(
                    categoryId = categoryId.value,
                    type = transactionType,
                    startDate = startDate,
                    endDate = endDate,
                )
            }
        )
    }

    @Test
    fun save() = runTest {
        // given
        store = newStore(fakeDao = FakeTransactionDao())
        val transaction = mockkFakeTransactionMapping()

        // when
        store.save(transaction)

        // then
        val savedTransaction = store.findById(transaction.id)
        savedTransaction shouldBe transaction
    }

    @Test
    fun saveMany() = runTest {
        // given
        store = newStore(fakeDao = FakeTransactionDao())
        val transaction1 = mockkFakeTransactionMapping()
        val transaction2 = mockkFakeTransactionMapping()

        // when
        store.saveMany(listOf(transaction1, transaction2))

        // then
        val savedTransactions = store.findAll()
        savedTransactions.toSet() shouldBe setOf(transaction1, transaction2)
    }

    @Test
    fun deleteById() = runTest {
        // given
        store = newStore(fakeDao = FakeTransactionDao())
        val transaction = mockkFakeTransactionMapping()
        store.save(transaction)

        // when
        store.deleteById(transaction.id)

        // then
        store.findById(transaction.id) shouldBe null
    }

    @Test
    fun deleteAllByAccountId() = runTest {
        // given
        store = newStore(fakeDao = FakeTransactionDao())
        val acc1 = Arb.accountId().next()
        val acc2 = Arb.accountId().next()
        val transactionOneAcc1 = mockkFakeTransactionMapping(account = acc1)
        val transactionTwoAcc1 = mockkFakeTransactionMapping(account = acc1)
        val transactionAcc2 = mockkFakeTransactionMapping(account = acc2)
        store.saveMany(listOf(transactionOneAcc1, transactionTwoAcc1, transactionAcc2))

        // when
        store.deleteAllByAccountId(accountId = acc1)

        // then
        store.findAll() shouldBe listOf(transactionAcc2)
    }

    @Test
    fun countNumberOfTransactions() = runTest {
        // given
        store = newStore(fakeDao = FakeTransactionDao())

        store.countHappenedTransactions().value shouldBeGreaterThanOrEqual 0L
    }

    @Test
    fun deleteAll() = runTest {
        // given
        store = newStore(fakeDao = FakeTransactionDao())
        val transaction1 = mockkFakeTransactionMapping()
        val transaction2 = mockkFakeTransactionMapping()
        val transaction3 = mockkFakeTransactionMapping()
        store.saveMany(listOf(transaction1, transaction2, transaction3))

        // when
        store.deleteAll()

        // then
        store.findAll() shouldBe emptyList()
    }

    private fun mockkFakeTransactionMapping(
        account: AccountId = Arb.accountId().next()
    ): Transaction {
        val transaction = Arb.transaction(account = Some(account)).next()
        val entity = mockk<TransactionEntity>(relaxed = true) {
            every { id } returns transaction.id.value
            every { accountId } returns account.value
        }
        with(mapper) {
            every { transaction.toEntity() } returns entity
            coEvery { entity.toDomain(any()) } returns Either.Right(transaction)
        }
        return transaction
    }

    private fun transactionsTestCase(
        daoMethod: suspend () -> List<TransactionEntity>,
        repoMethod: suspend () -> List<Transaction>,
        mapExpectedResult: (List<Transaction>) -> List<Transaction> = ::identity
    ) = runTest {
        checkAll(
            Arb.map(
                arb = Arb.transactionMappingRow(),
                minSize = 0,
                maxSize = 10,
            )
        ) { transactionMapping ->
            // given
            coEvery { daoMethod() } returns transactionMapping.keys.toList()
            transactionMapping.forEach { (entity, mappingRes) ->
                with(mapper) {
                    coEvery { entity.toDomain(any()) } returns mappingRes
                }
            }

            // when
            val transactions = repoMethod()

            // then
            val expectedTransactions = transactionMapping.values.mapNotNull { it.getOrNull() }
            transactions.toSet() shouldBe mapExpectedResult(expectedTransactions).toSet()
        }
    }

    private fun Arb.Companion.transactionMappingRow(): Arb<TransactionMappingRow> = arbitrary {
        val isValid = Arb.boolean().bind()
        if (isValid) {
            Arb.validTransactionEntity().bind() to Either.Right(Arb.transaction().bind())
        } else {
            Arb.invalidTransactionEntity().bind() to Either.Left(Arb.string().bind())
        }
    }
}

typealias TransactionMappingRow = Pair<TransactionEntity, Either<String, Transaction>>
