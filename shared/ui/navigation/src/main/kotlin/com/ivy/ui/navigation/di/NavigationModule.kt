package com.ivy.ui.navigation.di

import com.ivy.ui.navigation.Navigation
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object NavigationModule {
    @Provides
    @Singleton
    fun navigation(): Navigation = Navigation()
}
