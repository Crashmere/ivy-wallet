package com.ivy.legacy

import android.view.View
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.ivy.base.legacy.Theme
import com.ivy.base.legacy.appContext
import com.ivy.design.IvyContext
import com.ivy.design.api.IvyDesign
import com.ivy.design.api.ivyContext
import com.ivy.design.api.systems.IvyWalletDesign
import com.ivy.design.utils.IvyPreview
import com.ivy.domain.RootScreen
import com.ivy.navigation.Navigation
import com.ivy.navigation.NavigationRoot

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun ivyWalletCtx(): IvyWalletCtx = ivyContext() as IvyWalletCtx

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun rootView(): View = LocalView.current

@Composable
fun rootScreen(): RootScreen = LocalContext.current as RootScreen

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun IvyWalletPreview(
    theme: Theme = Theme.LIGHT,
    content: @Composable BoxWithConstraintsScope.() -> Unit
) {
    appContext = rootView().context
    IvyPreview(
        theme = theme,
        design = appDesign(IvyWalletCtx()),
    ) {
        NavigationRoot(navigation = Navigation()) {
            content()
        }
    }
}

@Deprecated("Old design system. Use `:ivy-design` and Material3")
fun appDesign(context: IvyWalletCtx): IvyDesign = object : IvyWalletDesign() {
    override fun context(): IvyContext = context
}
