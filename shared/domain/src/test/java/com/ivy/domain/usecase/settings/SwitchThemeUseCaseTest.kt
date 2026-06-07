package com.ivy.domain.usecase.settings

import com.ivy.base.theme.Theme
import com.ivy.data.api.SettingsStore
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.math.BigDecimal

class SwitchThemeUseCaseTest {

    private val store = FakeSettingsStore(initialTheme = Theme.LIGHT)
    private val useCase = SwitchThemeUseCase(store)

    @Test
    fun cyclesThemesInDomainOrder() = runTest {
        useCase() shouldBe Theme.DARK
        useCase() shouldBe Theme.AMOLED_DARK
        useCase() shouldBe Theme.AUTO
        useCase() shouldBe Theme.LIGHT
    }

    private class FakeSettingsStore(
        initialTheme: Theme,
    ) : SettingsStore {
        private var theme = initialTheme

        override suspend fun getTheme(fallback: Theme): Theme = theme

        override suspend fun getTheme(systemDarkMode: Boolean): Theme = theme

        override suspend fun ensureInitialized(
            systemDarkMode: Boolean,
            currencyCode: String,
            bufferAmount: Double,
        ) = Unit

        override suspend fun setTheme(theme: Theme): Theme {
            this.theme = theme
            return theme
        }

        override suspend fun getBufferAmount(): BigDecimal = BigDecimal.ZERO

        override suspend fun setBufferAmount(amount: BigDecimal): BigDecimal = amount

        override suspend fun deleteAll() = Unit
    }
}
