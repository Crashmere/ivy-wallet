package com.ivy.ui.navigation

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainTabState @Inject constructor() {
    var selectedTab by mutableStateOf(MainTab.HOME)
        private set

    fun select(tab: MainTab) {
        selectedTab = tab
    }
}

@Suppress("CompositionLocalAllowlist")
val LocalMainTabState = compositionLocalOf<MainTabState> { error("No LocalMainTabState") }
