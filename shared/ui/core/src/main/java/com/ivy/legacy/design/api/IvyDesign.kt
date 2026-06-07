package com.ivy.legacy.design.api

import com.ivy.base.legacy.Theme
import com.ivy.legacy.design.l0_system.IvyColors
import com.ivy.legacy.design.l0_system.IvyShapes
import com.ivy.legacy.design.l0_system.IvyTypography

@Deprecated("Old design system. Use `:ivy-design` and Material3")
interface IvyDesign {
    @Deprecated("Old design system. Use `:ivy-design` and Material3")
    fun typography(): IvyTypography

    @Deprecated("Old design system. Use `:ivy-design` and Material3")
    fun colors(theme: Theme, isDarkModeEnabled: Boolean): IvyColors

    @Deprecated("Old design system. Use `:ivy-design` and Material3")
    fun shapes(): IvyShapes
}
