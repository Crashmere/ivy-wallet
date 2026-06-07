package com.ivy.legacy.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.ivy.domain.RootScreen

@Composable
fun rootScreen(): RootScreen = LocalContext.current as RootScreen
