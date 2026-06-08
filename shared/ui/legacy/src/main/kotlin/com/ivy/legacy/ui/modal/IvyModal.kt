package com.ivy.legacy.ui.modal

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.ivy.legacy.ui.theme.system.LegacyTheme
import com.ivy.ui.compose.rememberInteractionSource
import com.ivy.legacy.ui.addKeyboardListener
import com.ivy.ui.compose.consumeClicks
import com.ivy.ui.compose.densityScope
import com.ivy.legacy.ui.hideKeyboard
import com.ivy.legacy.ui.keyboardOnlyWindowInsets
import com.ivy.legacy.ui.navigationBarInsets
import com.ivy.legacy.ui.onScreenStart
import com.ivy.ui.compose.thenIf
import com.ivy.ui.navigation.navigation
import com.ivy.legacy.ui.component.ActionsRow
import com.ivy.legacy.ui.component.CloseButton
import com.ivy.legacy.ui.theme.gradientCutBackgroundTop
import com.ivy.legacy.ui.theme.mediumBlur
import java.util.UUID
import kotlin.math.roundToInt
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

private const val DURATION_BACKGROUND_BLUR_ANIM = 400

const val DURATION_MODAL_ANIM = 200

@Composable
fun BoxScope.IvyModal(
    id: UUID?,
    visible: Boolean,
    dismiss: () -> Unit,
    SecondaryActions: (@Composable () -> Unit)? = null,
    PrimaryAction: @Composable () -> Unit,
    scrollState: ScrollState? = rememberScrollState(),
    shiftIfKeyboardShown: Boolean = true,
    includeActionsRowPadding: Boolean = true,
    Content: @Composable ColumnScope.() -> Unit
) {
    val rootView = LocalView.current
    var keyboardShown by remember { mutableStateOf(false) }

    onScreenStart {
        rootView.addKeyboardListener {
            keyboardShown = it
        }
    }

    val keyboardShownInsetDp by animateDpAsState(
        targetValue = densityScope {
            if (keyboardShown) keyboardOnlyWindowInsets().bottom.toDp() else 0.dp
        },
        animationSpec = tween(DURATION_MODAL_ANIM)
    )
    val navBarPadding by animateDpAsState(
        targetValue = densityScope {
            if (keyboardShown) 0.dp else navigationBarInsets().bottom.toDp()
        },
        animationSpec = tween(DURATION_MODAL_ANIM)
    )
    val blurAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(DURATION_BACKGROUND_BLUR_ANIM),
        visibilityThreshold = 0.01f
    )
    val modalPercentVisible by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(DURATION_MODAL_ANIM),
        visibilityThreshold = 0.01f
    )

    if (visible || blurAlpha > 0.01f) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(blurAlpha)
                .background(mediumBlur())
                .testTag("modal_outside_blur")
                .clickable(
                    onClick = {
                        hideKeyboard(rootView)
                        dismiss()
                    },
                    enabled = visible
                )
                .zIndex(1000f)
        )
    }

    if (visible || modalPercentVisible > 0.01f) {
        var actionsRowHeight by remember { mutableIntStateOf(0) }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)

                    val height = placeable.height
                    val y = height * (1 - modalPercentVisible)

                    layout(placeable.width, height) {
                        placeable.placeRelative(
                            0,
                            y.roundToInt()
                        )
                    }
                }
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 24.dp)
                .background(LegacyTheme.colors.pure, LegacyTheme.shapes.r2Top)
                .consumeClicks(rememberInteractionSource())
                .thenIf(scrollState != null) {
                    verticalScroll(scrollState!!)
                }
                .zIndex(1000f)
        ) {
            ModalBackHandling(
                modalId = id,
                visible = visible,
                dismiss = dismiss
            )

            Content()

            // Bottom padding
            if (includeActionsRowPadding) {
                Spacer(Modifier.height(densityScope { actionsRowHeight.toDp() }))
            }

            if (shiftIfKeyboardShown) {
                Spacer(Modifier.height(keyboardShownInsetDp))
            }
        }

        ModalActionsRow(
            visible = visible,
            modalPercentVisible = modalPercentVisible,
            keyboardShownInsetDp = keyboardShownInsetDp,
            navBarPadding = navBarPadding,
            onHeightChanged = {
                actionsRowHeight = it
            },
            onClose = {
                hideKeyboard(rootView)
                dismiss()
            },
            SecondaryActions = SecondaryActions,
            PrimaryAction = PrimaryAction
        )
    }
}

@Composable
private fun ModalBackHandling(
    modalId: UUID?,
    visible: Boolean,
    dismiss: () -> Unit
) {
    AddModalBackHandling(
        modalId = modalId,
        visible = visible,
        action = {
            dismiss()
        }
    )
}

@Composable
fun AddModalBackHandling(
    modalId: UUID?,
    visible: Boolean,
    action: () -> Unit
) {
    val latestAction by rememberUpdatedState(action)
    val nav = navigation()
    DisposableEffect(visible) {
        if (visible && modalId != null) {
            nav.addModalBackHandler(
                id = modalId,
                onBackPressed = {
                    if (visible) {
                        latestAction()
                        true
                    } else {
                        false
                    }
                }
            )
        }

        onDispose {
            if (modalId != null) {
                nav.removeModalBackHandler(modalId)
            }
        }
    }
}

@Suppress("ParameterNaming")
@Composable
fun ModalActionsRow(
    visible: Boolean,
    modalPercentVisible: Float,
    keyboardShownInsetDp: Dp,
    navBarPadding: Dp,

    onHeightChanged: (Int) -> Unit,

    onClose: () -> Unit,
    SecondaryActions: (@Composable () -> Unit)? = null,
    PrimaryAction: @Composable () -> Unit
) {
    if (visible || modalPercentVisible > 0.01f) {
        val screenHeight = LocalConfiguration.current.screenHeightDp.dp
        ActionsRow(
            modifier = Modifier
                .onSizeChanged {
                    onHeightChanged(it.height)
                }
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)

                    val systemOffsetBottom = keyboardShownInsetDp.toPx()
                    val visibleHeight = placeable.height * modalPercentVisible
                    val y = screenHeight.toPx() - visibleHeight - systemOffsetBottom

                    layout(placeable.width, placeable.height) {
                        placeable.place(
                            0,
                            y.roundToInt()
                        )
                    }
                }
                .gradientCutBackgroundTop(
                    pure = LegacyTheme.colors.pure,
                    density = LocalDensity.current,
                    endY = 16.dp
                )
                .padding(top = 8.dp, bottom = 12.dp)
                .padding(bottom = navBarPadding)
                .zIndex(1100f)
        ) {
            Spacer(Modifier.width(24.dp))

            CloseButton(
                modifier = Modifier.testTag("modal_close_button"),
                onClick = onClose
            )

            SecondaryActions?.invoke()

            Spacer(Modifier.weight(1f))

            PrimaryAction()

            Spacer(Modifier.width(24.dp))
        }
    }
}

