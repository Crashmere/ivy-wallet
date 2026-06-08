package com.ivy.ui.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Stable
class Navigation {
  var currentScreen: Screen? by mutableStateOf(null)
    private set

  private val screenBackHandlers: MutableMap<Screen, () -> Boolean> = mutableMapOf()

  private val backStack = ArrayDeque<Screen>()
  var lastScreen: Screen? = null
    private set

  fun registerScreenBackHandler(screen: Screen, handler: () -> Boolean) {
    screenBackHandlers[screen] = handler
  }

  fun unregisterScreenBackHandler(screen: Screen) {
    screenBackHandlers.remove(screen)
  }

  fun navigateTo(screen: Screen) {
    val previousScreen = lastScreen
    if (previousScreen != null) {
      backStack.addLast(previousScreen)
    }
    switchScreen(screen)
  }

  fun backStackEmpty() = backStack.isEmpty()

  private fun popBackStack() {
    backStack.removeLast()
  }

  fun handleRootBack(): Boolean {
    val specialHandling = screenBackHandlers.getOrDefault(currentScreen) { false }.invoke()
    return specialHandling || back()
  }

  fun back(): Boolean {
    if (backStack.isNotEmpty()) {
      switchScreen(backStack.removeLast())
      return true
    }
    return false
  }

  private fun switchScreen(screen: Screen) {
    this.currentScreen = screen
    lastScreen = screen
  }

  fun resetBackStack() {
    while (!backStackEmpty()) {
      popBackStack()
    }
    lastScreen = null
  }
}
