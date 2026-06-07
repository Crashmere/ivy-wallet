package com.ivy.domain.usecase.csv

import arrow.core.Some
import com.ivy.base.time.impl.TestTimeConverter
import com.ivy.data.api.AccountStore
import com.ivy.data.api.CategoryStore
import com.ivy.data.api.TransactionStore
import com.ivy.data.api.file.TextFileStore
import com.ivy.data.model.Transaction
import com.ivy.data.model.getFromAccount
import com.ivy.data.model.getToAccount
import com.ivy.data.model.testing.account
import com.ivy.data.model.testing.category
import com.ivy.data.model.testing.transaction
import com.ivy.domain.usecase.account.GetAccountsUseCase
import com.ivy.domain.usecase.category.GetCategoriesUseCase
import com.ivy.domain.usecase.transaction.GetTransactionsUseCase
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.next
import io.kotest.property.checkAll
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ExportCsvUseCasePropertyTest {

    private val accountStore = mockk<AccountStore>()
    private val categoryStore = mockk<CategoryStore>(relaxed = true)
    private val transactionStore = mockk<TransactionStore>()
    private val textFileStore = mockk<TextFileStore>()
    private val timeConverter = TestTimeConverter()

    private lateinit var useCase: ExportCsvUseCase

    @Before
    fun setup() {
        useCase = ExportCsvUseCase(
            getAccountsUseCase = GetAccountsUseCase(accountStore),
            getCategoriesUseCase = GetCategoriesUseCase(categoryStore),
            getTransactionsUseCase = GetTransactionsUseCase(transactionStore),
            textFileStore = textFileStore,
            timeConverter = timeConverter
        )
    }

    @Test
    fun `property - num of row and columns matches the format`() = runTest {
        checkAll(Arb.list(Arb.transaction())) { trns ->
            // given
            val accounts = trns.flatMap {
                listOfNotNull(it.getFromAccount(), it.getToAccount())
            }.map {
                Arb.account(accountId = Some(it)).next()
            }
            coEvery { accountStore.findAll() } returns accounts
            val categories = trns
                .mapNotNull(Transaction::category)
                .map {
                    Arb.category(categoryId = Some(it)).next()
                }.run {
                    if (isNotEmpty()) {
                        drop(Arb.int(indices).bind()).shuffled()
                    } else {
                        this
                    }
                }
            coEvery { categoryStore.findAll() } returns categories

            // when
            val csv = useCase.exportCsv { trns }

            // then
            val rows = CsvTestReader().readCsv(csv)
            rows.size shouldBe trns.size + 1 // +1 for the header
            rows.forEach { row ->
                // Matches the expected # of columns
                val hasExpectedNumOfColumns = row.size == IvyCsvRow.Columns.size
                if (!hasExpectedNumOfColumns) {
                    println("(${row.size} cols) $row")
                }
                hasExpectedNumOfColumns shouldBe true
            }
        }
    }
}
