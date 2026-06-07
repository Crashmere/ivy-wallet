package com.ivy.domain.usecase.settings

import com.ivy.base.theme.Theme
import com.ivy.data.api.SettingsStore
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.math.BigDecimal

class SwitchThemeUseCaseTest {

    @Test
    fun cyclesThemesInDomainOrder() = runTest {
        val store = FakeSettingsStore(initialTheme = Theme.LIGHT)
        val useCase = SwitchThemeUseCase(store)

        useCase() shouldBe Theme.DARK
        useCase() shouldBe Theme.AMOLED_DARK
        useCase() shouldBe Theme.AUTO
        useCase() shouldBe Theme.LIGHT
    }

    @Test
    fun usesSystemDarkModeAsThemeFallback() = runTest {
        val useCase = GetThemeUseCase(FakeSettingsStore(initialTheme = null))

        useCase.withSystemFallback(systemDarkMode = true) shouldBe Theme.DARK
        useCase.withSystemFallback(systemDarkMode = false) shouldBe Theme.LIGHT
    }

    @Test
    fun mapsSystemDarkModeToInitialTheme() = runTest {
        val darkStore = FakeSettingsStore(initialTheme = null)
        val lightStore = FakeSettingsStore(initialTheme = null)

        EnsureSettingsInitializedUseCase(darkStore)(
            systemDarkMode = true,
            currencyCode = "USD",
            bufferAmount = 1000.0,
        )
        EnsureSettingsInitializedUseCase(lightStore)(
            systemDarkMode = false,
            currencyCode = "USD",
            bufferAmount = 1000.0,
        )

        darkStore.initializedTheme shouldBe Theme.DARK
        lightStore.initializedTheme shouldBe Theme.LIGHT
    }

    private class FakeSettingsStore(
        initialTheme: Theme?,
    ) : SettingsStore {
        private var theme = initialTheme
        var initializedTheme: Theme? = null

        override suspend fun getTheme(fallback: Theme): Theme = theme ?: fallback

        override suspend fun ensureInitialized(
            defaultTheme: Theme,
            currencyCode: String,
            bufferAmount: Double,
        ) {
            initializedTheme = defaultTheme
            if (theme == null) {
                theme = defaultTheme
            }
        }

        override suspend fun setTheme(theme: Theme): Theme {
            this.theme = theme
            return theme
        }

        override suspend fun getBufferAmount(): BigDecimal = BigDecimal.ZERO

        override suspend fun setBufferAmount(amount: BigDecimal): BigDecimal = amount

        override suspend fun deleteAll() = Unit
    }
}
