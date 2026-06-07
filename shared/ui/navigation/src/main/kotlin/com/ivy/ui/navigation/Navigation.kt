package com.ivy.ui.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Stack
import java.util.UUID

@Stable
class Navigation {
  var currentScreen: Screen? by mutableStateOf(null)
    private set

  private val modalBackHandlers: Stack<ModalBackHandler> = Stack()

  private val screenBackHandlers: MutableMap<Screen, () -> Boolean> = mutableMapOf()

  private val backStack: Stack<Screen> = Stack()
  var lastScreen: Screen? = null
    private set

  private data class ModalBackHandler(
    val id: UUID,
    val onBackPressed: () -> Boolean
  )

  fun registerScreenBackHandler(screen: Screen, handler: () -> Boolean) {
    screenBackHandlers[screen] = handler
  }

  fun addModalBackHandler(
    id: UUID,
    onBackPressed: () -> Boolean
  ) {
    if (modalBackHandlers.lastOrNull()?.id != id) {
      modalBackHandlers.add(
        ModalBackHandler(
          id = id,
          onBackPressed = onBackPressed
        )
      )
    }
  }

  fun removeModalBackHandler(id: UUID) {
    if (modalBackHandlers.lastOrNull()?.id == id) {
      modalBackHandlers.pop()
    }
  }

  fun navigateTo(screen: Screen) {
    if (lastScreen != null) {
      backStack.push(lastScreen)
    }
    switchScreen(screen)
  }

  fun backStackEmpty() = backStack.empty()

  private fun popBackStack() {
    backStack.pop()
  }

  fun handleRootBack(): Boolean {
    if (modalBackHandlers.isNotEmpty()) {
      return modalBackHandlers.peek().onBackPressed()
    }
    val specialHandling = screenBackHandlers.getOrDefault(currentScreen) { false }.invoke()
    return specialHandling || back()
  }

  fun back(): Boolean {
    if (!backStack.empty()) {
      switchScreen(backStack.pop())
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
