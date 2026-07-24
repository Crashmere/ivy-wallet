package com.ivy.ui.compose

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

/**
 * Long-press-then-drag reordering for a [androidx.compose.foundation.lazy.LazyColumn].
 *
 * Wrap each reorderable row with [DraggableItem], giving it the row's stable [key] and its
 * LazyColumn item index. The long-press drag gesture lives on the row itself (not the list),
 * so it wins over the list's own scroll once the long press fires. [draggable] restricts which
 * indices can be picked up (useful when the list mixes fixed header/footer items with
 * reorderable rows); [onMove] is invoked live during the drag with the LazyColumn item indices
 * being swapped, so the caller can mutate its backing list and let the list animate the change.
 */
@Composable
fun rememberDragDropState(
    lazyListState: LazyListState,
    draggable: (index: Int) -> Boolean = { true },
    onMove: (fromIndex: Int, toIndex: Int) -> Unit,
): DragDropState {
    val scope = rememberCoroutineScope()
    val state = remember(lazyListState) {
        DragDropState(
            state = lazyListState,
            scope = scope,
            draggable = draggable,
            onMove = onMove,
        )
    }
    LaunchedEffect(state) {
        while (true) {
            val diff = state.scrollChannel.receive()
            lazyListState.scrollBy(diff)
        }
    }
    return state
}

class DragDropState internal constructor(
    private val state: LazyListState,
    private val scope: CoroutineScope,
    private val draggable: (index: Int) -> Boolean,
    private val onMove: (Int, Int) -> Unit,
) {
    // Set while the row is picked up AND while it settles back into place on release, so the
    // row keeps its elevated, offset rendering for the whole gesture (no stale in-between frame).
    var draggingItemIndex by mutableStateOf<Int?>(null)
        private set

    internal val scrollChannel = Channel<Float>()

    private var draggingItemDraggedDelta by mutableFloatStateOf(0f)
    private var draggingItemInitialOffset by mutableIntStateOf(0)
    private var settleJob: Job? = null

    internal val draggingItemOffset: Float
        get() = draggingItemLayoutInfo?.let { item ->
            draggingItemInitialOffset + draggingItemDraggedDelta - item.offset
        } ?: 0f

    private val draggingItemLayoutInfo: LazyListItemInfo?
        get() = state.layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == draggingItemIndex }

    internal fun onDragStart(index: Int) {
        if (!draggable(index)) return
        val item = state.layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == index } ?: return
        settleJob?.cancel()
        draggingItemIndex = index
        draggingItemInitialOffset = item.offset
        draggingItemDraggedDelta = 0f
    }

    internal fun onDragInterrupted() {
        val item = draggingItemLayoutInfo
        if (item == null) {
            reset()
            return
        }
        // Animate the row from where the finger left it back into its slot. The visible offset
        // is derived live from [draggingItemDraggedDelta], so animating that value keeps the row
        // continuous across release instead of snapping for a frame.
        val targetDelta = (item.offset - draggingItemInitialOffset).toFloat()
        settleJob = scope.launch {
            animate(
                initialValue = draggingItemDraggedDelta,
                targetValue = targetDelta,
                animationSpec = spring(
                    stiffness = Spring.StiffnessMediumLow,
                    visibilityThreshold = 1f,
                ),
            ) { value, _ ->
                draggingItemDraggedDelta = value
            }
            reset()
        }
    }

    private fun reset() {
        draggingItemDraggedDelta = 0f
        draggingItemIndex = null
        draggingItemInitialOffset = 0
    }

    internal fun onDrag(offset: Offset) {
        draggingItemDraggedDelta += offset.y

        val draggingItem = draggingItemLayoutInfo ?: return
        val startOffset = draggingItem.offset + draggingItemOffset
        val endOffset = startOffset + draggingItem.size
        val middleOffset = startOffset + (endOffset - startOffset) / 2f

        val targetItem = state.layoutInfo.visibleItemsInfo.find { item ->
            middleOffset.toInt() in item.offset..(item.offset + item.size) &&
                draggingItem.index != item.index &&
                draggable(item.index)
        }

        if (targetItem != null) {
            onMove(draggingItem.index, targetItem.index)
            draggingItemIndex = targetItem.index
        } else {
            val overscroll = when {
                draggingItemDraggedDelta > 0 ->
                    (endOffset - state.layoutInfo.viewportEndOffset).coerceAtLeast(0f)

                draggingItemDraggedDelta < 0 ->
                    (startOffset - state.layoutInfo.viewportStartOffset).coerceAtMost(0f)

                else -> 0f
            }
            if (overscroll != 0f) {
                scrollChannel.trySend(overscroll)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyItemScope.DraggableItem(
    dragDropState: DragDropState,
    index: Int,
    key: Any,
    modifier: Modifier = Modifier,
    onDragFinished: () -> Unit = {},
    content: @Composable ColumnScope.(isDragging: Boolean) -> Unit,
) {
    val currentIndex by rememberUpdatedState(index)
    val dragging = index == dragDropState.draggingItemIndex
    val draggingModifier = when {
        dragging -> Modifier
            .zIndex(1f)
            .graphicsLayer { translationY = dragDropState.draggingItemOffset }
        // Only reflow-animate siblings while a drag is actually in progress. Otherwise a plain
        // content-size change elsewhere in the list (e.g. an expandable row collapsing) makes some
        // rows animate their placement while freshly-composed rows snap into place — looking janky.
        // When idle we let the list re-lay-out naturally so the whole column moves together.
        dragDropState.draggingItemIndex != null -> Modifier.animateItemPlacement()
        else -> Modifier
    }
    Column(
        modifier = modifier
            .then(draggingModifier)
            // Keyed on the stable row key (not the index) so reordering mid-drag doesn't
            // restart the gesture. The current index is read live via rememberUpdatedState.
            .pointerInput(key) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { dragDropState.onDragStart(currentIndex) },
                    onDrag = { change, offset ->
                        change.consume()
                        dragDropState.onDrag(offset)
                    },
                    onDragEnd = {
                        val wasDragging = dragDropState.draggingItemIndex != null
                        dragDropState.onDragInterrupted()
                        if (wasDragging) onDragFinished()
                    },
                    onDragCancel = { dragDropState.onDragInterrupted() },
                )
            },
    ) {
        content(dragging)
    }
}
