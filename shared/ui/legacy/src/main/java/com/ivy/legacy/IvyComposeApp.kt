package com.ivy.legacy

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.ivy.design.api.ivyContext
import com.ivy.domain.RootScreen

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun ivyWalletCtx(): IvyWalletCtx = ivyContext() as IvyWalletCtx

@Composable
fun rootScreen(): RootScreen = LocalContext.current as RootScreen
