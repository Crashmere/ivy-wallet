package com.ivy.data.store

import com.ivy.data.db.dao.fake.FakeSettingsDao
import com.ivy.data.db.entity.SettingsEntity
import com.ivy.data.model.Theme
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SettingsTableTest {
    private val settingsDao = FakeSettingsDao()
    private val table = SettingsTable(
        settingsDao = settingsDao,
        writeSettingsDao = settingsDao,
    )

    @Test
    fun `find or default returns local defaults when the table is empty`() = runTest {
        val settings = table.findOrDefault()

        settings.theme shouldBe Theme.AUTO
        settings.currency shouldBe LocalSettingsDefaults.FALLBACK_CURRENCY_CODE
        settings.bufferAmount shouldBe 0.0
    }

    @Test
    fun `ensure initialized creates the settings row once`() = runTest {
        table.ensureInitialized(
            defaultTheme = Theme.DARK,
            baseCurrencyCode = "EUR",
            bufferAmount = 12.5,
        )

        table.ensureInitialized(
            defaultTheme = Theme.LIGHT,
            baseCurrencyCode = "GBP",
            bufferAmount = 99.0,
        )

        val settings = settingsDao.findAll()
        settings.shouldHaveSize(1)
        settings.first().theme shouldBe Theme.DARK
        settings.first().currency shouldBe "EUR"
        settings.first().bufferAmount shouldBe 12.5
    }

    @Test
    fun `save updates the same row`() = runTest {
        val original = SettingsEntity(
            theme = Theme.LIGHT,
            currency = "USD",
            bufferAmount = 0.0,
        )
        table.save(original)

        table.save(
            original.copy(
                theme = Theme.AMOLED_DARK,
                currency = "EUR",
                bufferAmount = 8.0,
            )
        )

        val settings = settingsDao.findAll()
        settings.shouldHaveSize(1)
        settings.first().theme shouldBe Theme.AMOLED_DARK
        settings.first().currency shouldBe "EUR"
        settings.first().bufferAmount shouldBe 8.0
    }

    @Test
    fun `delete all clears the settings row`() = runTest {
        table.save(
            SettingsEntity(
                theme = Theme.LIGHT,
                currency = "USD",
                bufferAmount = 0.0,
            )
        )

        table.deleteAll()

        settingsDao.findAll() shouldBe emptyList()
    }
}
