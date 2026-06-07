package com.ivy.data.store

import com.ivy.data.db.dao.fake.FakeSettingsDao
import com.ivy.data.db.entity.SettingsEntity
import com.ivy.data.model.Theme
import com.ivy.data.model.primitive.AssetCode
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RoomCurrencyStoreTest {
    private val settingsDao = FakeSettingsDao()
    private val currencyStore = RoomCurrencyStore(
        settingsTable = SettingsTable(
            settingsDao = settingsDao,
            writeSettingsDao = settingsDao,
        )
    )

    @Test
    fun `get base currency returns stored currency`() = runTest {
        settingsDao.save(
            SettingsEntity(
                theme = Theme.LIGHT,
                currency = "EUR",
                bufferAmount = 0.0,
            )
        )

        currencyStore.getBaseCurrency() shouldBe AssetCode.EUR
        currencyStore.getBaseCurrencyCode() shouldBe "EUR"
    }

    @Test
    fun `get base currency falls back to USD when stored currency is invalid`() = runTest {
        settingsDao.save(
            SettingsEntity(
                theme = Theme.LIGHT,
                currency = " ",
                bufferAmount = 0.0,
            )
        )

        currencyStore.getBaseCurrency() shouldBe AssetCode.USD
    }

    @Test
    fun `set base currency preserves theme and buffer amount`() = runTest {
        settingsDao.save(
            SettingsEntity(
                theme = Theme.AMOLED_DARK,
                currency = "USD",
                bufferAmount = 19.0,
            )
        )

        currencyStore.setBaseCurrency(AssetCode.GBP)

        val settings = settingsDao.findFirst()
        settings.theme shouldBe Theme.AMOLED_DARK
        settings.currency shouldBe "GBP"
        settings.bufferAmount shouldBe 19.0
    }
}
