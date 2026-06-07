package com.ivy.ui.navigation

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class MainTabState {
    var selectedTab by mutableStateOf(MainTab.HOME)
        private set

    fun select(tab: MainTab) {
        selectedTab = tab
    }
}

@Suppress("CompositionLocalAllowlist")
val LocalMainTabState = compositionLocalOf<MainTabState> { error("No LocalMainTabState") }
