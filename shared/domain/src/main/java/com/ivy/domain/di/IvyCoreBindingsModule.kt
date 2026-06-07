package com.ivy.domain.di

import com.ivy.domain.preferences.toggles.PreferenceToggles
import com.ivy.domain.preferences.toggles.IvyPreferenceToggles
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface IvyCoreBindingsModule {
    @Binds
    fun bindPreferenceToggles(preferenceToggles: IvyPreferenceToggles): PreferenceToggles
}
