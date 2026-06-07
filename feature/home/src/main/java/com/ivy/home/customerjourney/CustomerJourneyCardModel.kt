package com.ivy.home.customerjourney

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import com.ivy.legacy.ui.theme.system.Gradient
import com.ivy.navigation.MainTabState
import com.ivy.navigation.Navigation

@Immutable
data class CustomerJourneyCardModel(
    val id: String,
    @Suppress("MaximumLineLength", "ParameterWrapping", "MaxLineLength", "ParameterListWrapping")
    val condition: suspend (trnCount: Long, plannedPaymentsCount: Long) -> Boolean,
    val title: String,
    val description: String,
    val cta: String?,
    @DrawableRes val ctaIcon: Int,

    val hasDismiss: Boolean = true,

    val background: Gradient,
    val onAction: (Navigation, MainTabState) -> Unit
)
