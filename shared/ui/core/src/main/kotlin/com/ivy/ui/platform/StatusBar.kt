package com.ivy.ui.platform

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView

@SuppressLint("ComposableNaming")
@Composable
fun setStatusBarDarkTextCompat(darkText: Boolean) {
    setStatusBarDarkTextCompat(
        view = LocalView.current,
        darkText = darkText
    )
}

fun setStatusBarDarkTextCompat(view: View, darkText: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        view.windowInsetsController?.setStatusBarDarkText(darkText)
    } else {
        val window = (view.context as Activity).window
        setStatusBarDarkTextOld(window, darkText)
    }
}

@RequiresApi(Build.VERSION_CODES.R)
fun WindowInsetsController.setStatusBarDarkText(darkText: Boolean) {
    setSystemBarsAppearance(
        if (darkText) WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0,
        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
    )
}

@Suppress("DEPRECATION")
fun setStatusBarDarkTextOld(window: Window, darkText: Boolean) {
    window.decorView.systemUiVisibility = if (darkText) {
        window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    } else {
        window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
    }
}
