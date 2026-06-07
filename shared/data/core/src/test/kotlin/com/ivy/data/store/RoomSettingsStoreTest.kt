package com.ivy.data.store

import com.ivy.data.db.dao.fake.FakeSettingsDao
import com.ivy.data.db.entity.SettingsEntity
import com.ivy.data.model.Theme
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.math.BigDecimal

class RoomSettingsStoreTest {
    private val settingsDao = FakeSettingsDao()
    private val settingsStore = RoomSettingsStore(
        settingsTable = SettingsTable(
            settingsDao = settingsDao,
            writeSettingsDao = settingsDao,
        )
    )

    @Test
    fun `get theme returns fallback when no settings row exists`() = runTest {
        settingsStore.getTheme(fallback = Theme.DARK) shouldBe Theme.DARK
    }

    @Test
    fun `ensure initialized stores provided defaults`() = runTest {
        settingsStore.ensureInitialized(
            defaultTheme = Theme.LIGHT,
            currencyCode = "EUR",
            bufferAmount = 3.5,
        )

        val settings = settingsDao.findFirst()
        settings.theme shouldBe Theme.LIGHT
        settings.currency shouldBe "EUR"
        settings.bufferAmount shouldBe 3.5
    }

    @Test
    fun `set theme preserves currency and buffer amount`() = runTest {
        settingsDao.save(
            SettingsEntity(
                theme = Theme.LIGHT,
                currency = "GBP",
                bufferAmount = 7.0,
            )
        )

        settingsStore.setTheme(Theme.AMOLED_DARK) shouldBe Theme.AMOLED_DARK

        val settings = settingsDao.findFirst()
        settings.theme shouldBe Theme.AMOLED_DARK
        settings.currency shouldBe "GBP"
        settings.bufferAmount shouldBe 7.0
    }

    @Test
    fun `get buffer amount returns zero when no settings row exists`() = runTest {
        settingsStore.getBufferAmount() shouldBe BigDecimal.ZERO
    }

    @Test
    fun `set buffer amount preserves theme and currency`() = runTest {
        settingsDao.save(
            SettingsEntity(
                theme = Theme.DARK,
                currency = "EUR",
                bufferAmount = 1.0,
            )
        )

        settingsStore.setBufferAmount(BigDecimal("12.5")) shouldBe BigDecimal("12.5")

        val settings = settingsDao.findFirst()
        settings.theme shouldBe Theme.DARK
        settings.currency shouldBe "EUR"
        settings.bufferAmount shouldBe 12.5
    }

    @Test
    fun `delete all clears settings`() = runTest {
        settingsDao.save(
            SettingsEntity(
                theme = Theme.DARK,
                currency = "EUR",
                bufferAmount = 1.0,
            )
        )

        settingsStore.deleteAll()

        settingsDao.findAll() shouldBe emptyList()
    }
}
