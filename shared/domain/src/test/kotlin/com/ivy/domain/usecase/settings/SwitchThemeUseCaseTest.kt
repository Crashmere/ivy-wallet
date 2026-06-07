package com.ivy.domain.usecase.settings

import com.ivy.data.api.SettingsInitializationStore
import com.ivy.data.api.ThemeStore
import com.ivy.data.model.Theme
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.Test

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
            baseCurrencyCode = "USD",
            bufferAmount = 1000.0,
        )
        EnsureSettingsInitializedUseCase(lightStore)(
            systemDarkMode = false,
            baseCurrencyCode = "USD",
            bufferAmount = 1000.0,
        )

        darkStore.initializedTheme shouldBe Theme.DARK
        lightStore.initializedTheme shouldBe Theme.LIGHT
    }

    private class FakeSettingsStore(
        initialTheme: Theme?,
    ) : SettingsInitializationStore, ThemeStore {
        private var theme = initialTheme
        var initializedTheme: Theme? = null

        override suspend fun getTheme(fallback: Theme): Theme = theme ?: fallback

        override suspend fun ensureInitialized(
            defaultTheme: Theme,
            baseCurrencyCode: String,
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

    }
}
