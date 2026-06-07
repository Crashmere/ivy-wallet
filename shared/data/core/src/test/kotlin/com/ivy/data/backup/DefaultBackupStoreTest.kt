package com.ivy.data.backup

import com.ivy.data.DataWriteEventBus
import com.ivy.data.api.BackupSettingsPreferenceStore
import com.ivy.data.db.dao.fake.FakeAccountDao
import com.ivy.data.db.dao.fake.FakeBudgetDao
import com.ivy.data.db.dao.fake.FakeCategoryDao
import com.ivy.data.db.dao.fake.FakeLoanDao
import com.ivy.data.db.dao.fake.FakeLoanRecordDao
import com.ivy.data.db.dao.fake.FakePlannedPaymentDao
import com.ivy.data.db.dao.fake.FakeSettingsDao
import com.ivy.data.db.dao.fake.FakeTagAssociationDao
import com.ivy.data.db.dao.fake.FakeTagDao
import com.ivy.data.db.dao.fake.FakeTransactionDao
import com.ivy.data.di.SerializationModule
import com.ivy.data.store.RoomAccountStore
import com.ivy.data.store.RoomCurrencyStore
import com.ivy.data.store.SettingsTable
import com.ivy.data.store.fake.fakeStoreCacheFactory
import com.ivy.data.mapper.AccountMapper
import com.ivy.data.testResource
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultBackupStoreTest {
    private fun newDefaultBackupStore(
        accountDao: FakeAccountDao = FakeAccountDao(),
        categoryDao: FakeCategoryDao = FakeCategoryDao(),
        transactionDao: FakeTransactionDao = FakeTransactionDao(),
        plannedPaymentDao: FakePlannedPaymentDao = FakePlannedPaymentDao(),
        budgetDao: FakeBudgetDao = FakeBudgetDao(),
        settingsDao: FakeSettingsDao = FakeSettingsDao(),
        loanDao: FakeLoanDao = FakeLoanDao(),
        loanRecordDao: FakeLoanRecordDao = FakeLoanRecordDao(),
        tagDao: FakeTagDao = FakeTagDao(),
        tagAssociationDao: FakeTagAssociationDao = FakeTagAssociationDao()
    ): DefaultBackupStore {
        val accountMapper = AccountMapper(
            RoomCurrencyStore(
                settingsTable = SettingsTable(
                    settingsDao = settingsDao,
                    writeSettingsDao = settingsDao,
                )
            )
        )
        return DefaultBackupStore(
            accountDao = accountDao,
            accountMapper = accountMapper,
            accountStore = RoomAccountStore(
                accountDao = accountDao,
                writeAccountDao = accountDao,
                mapper = accountMapper,
                cacheFactory = fakeStoreCacheFactory(),
            ),
            budgetDao = budgetDao,
            categoryDao = categoryDao,
            loanRecordDao = loanRecordDao,
            loanDao = loanDao,
            plannedPaymentRuleDao = plannedPaymentDao,
            transactionDao = transactionDao,
            transactionWriter = transactionDao,
            settingsDao = settingsDao,
            categoryWriter = categoryDao,
            settingsWriter = settingsDao,
            budgetWriter = budgetDao,
            loanWriter = loanDao,
            loanRecordWriter = loanRecordDao,
            plannedPaymentRuleWriter = plannedPaymentDao,

            context = mockk(relaxed = true),
            settingsPreferenceStore = mockk<BackupSettingsPreferenceStore>(relaxed = true),
            json = SerializationModule.provideJson(),
            fileSystem = mockk(relaxed = true),
            dataChangePublisher = DataWriteEventBus(),
            tagsReader = tagDao,
            tagsWriter = tagDao,
            tagAssociationReader = tagAssociationDao,
            tagAssociationWriter = tagAssociationDao
        )
    }

    private suspend fun backupTestCase(backupVersion: String) {
        // given
        val originalBackupStore = newDefaultBackupStore()
        val backupJsonData = testResource("backups/$backupVersion.json")
            .readText(Charsets.UTF_16)

        // when
        val importedDataRes = originalBackupStore.importJson(backupJsonData, onProgress = {})

        // then
        importedDataRes.accountsImported shouldBeGreaterThan 0
        importedDataRes.transactionsImported shouldBeGreaterThan 0
        importedDataRes.categoriesImported shouldBeGreaterThan 0
        importedDataRes.failedRows.size shouldBe 0

        // Also - exporting and re-importing the data should work
        // given
        val exportedJson = originalBackupStore.generateJsonBackup()

        // when
        val freshBackupStore = newDefaultBackupStore()
        val reImportedDataRes = freshBackupStore.importJson(exportedJson, onProgress = {})
        // then
        reImportedDataRes shouldBe importedDataRes

        // Finally, exporting again should yield the same result
        freshBackupStore.generateJsonBackup() shouldBe exportedJson
    }

    @Test
    fun `backup compatibility with 450 (150)`() = runTest {
        backupTestCase("450-150")
    }
}
