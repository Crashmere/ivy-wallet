package com.ivy.wallet.di

import android.content.Context
import com.ivy.ui.period.PeriodState
import com.ivy.ui.platform.Toaster
import com.ivy.ui.resource.ResourceProvider
import com.ivy.ui.navigation.MainTabState
import com.ivy.ui.navigation.Navigation
import com.ivy.ui.theme.ThemeState
import com.ivy.ui.time.DateTimePicker
import com.ivy.ui.time.DevicePreferences
import com.ivy.ui.time.TimeConverter
import com.ivy.ui.time.TimeFormatter
import com.ivy.ui.time.TimeProvider
import com.ivy.ui.time.impl.AndroidDateTimePicker
import com.ivy.ui.time.impl.AndroidDevicePreferences
import com.ivy.ui.time.impl.DeviceTimeProvider
import com.ivy.ui.time.impl.IvyTimeFormatter
import com.ivy.ui.time.impl.StandardTimeConverter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UiCoreBindingsModule {
    @Provides
    @Singleton
    fun themeState(): ThemeState = ThemeState()

    @Provides
    @Singleton
    fun navigation(): Navigation = Navigation()

    @Provides
    @Singleton
    fun mainTabState(): MainTabState = MainTabState()

    @Provides
    @Singleton
    fun periodState(
        timeConverter: TimeConverter,
        timeProvider: TimeProvider,
    ): PeriodState = PeriodState(
        timeConverter = timeConverter,
        timeProvider = timeProvider,
    )

    @Provides
    fun timeProvider(): TimeProvider = DeviceTimeProvider()

    @Provides
    fun timeConverter(timeProvider: TimeProvider): TimeConverter {
        return StandardTimeConverter(timeProvider)
    }

    @Provides
    fun timeFormatter(
        resourceProvider: ResourceProvider,
        timeProvider: TimeProvider,
        timeConverter: TimeConverter,
        devicePreferences: DevicePreferences,
    ): TimeFormatter {
        return IvyTimeFormatter(
            resourceProvider = resourceProvider,
            timeProvider = timeProvider,
            converter = timeConverter,
            devicePreferences = devicePreferences,
        )
    }

    @Provides
    fun devicePreferences(
        @ApplicationContext context: Context
    ): DevicePreferences = AndroidDevicePreferences(context)

    @Provides
    @Singleton
    fun dateTimePicker(
        timeProvider: TimeProvider,
        timeConverter: TimeConverter,
    ): DateTimePicker = AndroidDateTimePicker(
        timeProvider = timeProvider,
        timeConverter = timeConverter,
    )

    @Provides
    fun toaster(
        @ApplicationContext context: Context,
        resourceProvider: ResourceProvider,
    ): Toaster = Toaster(
        context = context,
        resourceProvider = resourceProvider,
    )
}
