package com.ivy.ui.di

import com.ivy.ui.time.DevicePreferences
import com.ivy.ui.time.DateTimePicker
import com.ivy.ui.time.TimeConverter
import com.ivy.ui.time.TimeFormatter
import com.ivy.ui.time.TimeProvider
import com.ivy.ui.time.impl.AndroidDateTimePicker
import com.ivy.ui.time.impl.AndroidDevicePreferences
import com.ivy.ui.time.impl.DeviceTimeProvider
import com.ivy.ui.time.impl.IvyTimeFormatter
import com.ivy.ui.time.impl.StandardTimeConverter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface IvyUiBindings {
    @Binds
    fun timeProvider(impl: DeviceTimeProvider): TimeProvider

    @Binds
    fun timeConverter(impl: StandardTimeConverter): TimeConverter

    @Binds
    fun timeFormatter(impl: IvyTimeFormatter): TimeFormatter

    @Binds
    fun deviceTimePreferences(impl: AndroidDevicePreferences): DevicePreferences

    @Binds
    fun dateTimePicker(impl: AndroidDateTimePicker): DateTimePicker
}
