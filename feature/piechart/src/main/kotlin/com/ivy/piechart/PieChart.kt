package com.ivy.piechart

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.ivy.data.model.TransactionType
import com.ivy.data.model.Category
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.ui.compose.drawColoredShadow
import com.ivy.ui.R
import com.ivy.legacy.ui.theme.Gradient
import com.ivy.legacy.ui.theme.Gray
import com.ivy.legacy.ui.icon.IvyIcon
import com.ivy.legacy.ui.theme.toComposeColor
import kotlinx.collections.immutable.ImmutableList
import kotlin.math.acos
import kotlin.math.sqrt

private const val PIE_CHART_RADIUS_DP = 128
private const val RADIUS_DP = 112f
private val PieChartShadowColor = Color(0xFF111114)

@Composable
internal fun PieChart(
    type: TransactionType,
    categoryAmounts: ImmutableList<CategoryAmount>,
    selectedCategory: SelectedCategory?,
    modifier: Modifier = Modifier,
    onCategoryClick: (Category?) -> Unit = {}
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier
                .size((PIE_CHART_RADIUS_DP * 2).dp)
                .drawColoredShadow(
                    color = PieChartShadowColor,
                    alpha = if (LegacyTheme.colors.isLight) 0.05f else 0.5f,
                    offsetY = 32.dp,
                    shadowRadius = 48.dp
                )
                .clip(CircleShape)
                .background(
                    brush = Gradient(
                        LegacyTheme.colors.medium,
                        LegacyTheme.colors.pure
                    ).asVerticalBrush(),
                    shape = CircleShape
                )
                .padding(all = 16.dp),
            factory = {
                PieChartView(it)
            },
            update = { view ->
                view.display(
                    categoryAmounts = categoryAmounts.sortedByDescending { it.amount },
                    selectedCategory = selectedCategory,
                    onCategoryClicked = onCategoryClick
                )
            }
        )

        IvyIcon(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(LegacyTheme.colors.medium)
                .padding(all = 20.dp),
            icon = if (type == TransactionType.INCOME) R.drawable.ic_income else R.drawable.ic_expense,
            tint = Gray
        )
    }
}

private class PieChartView(context: Context) : View(context) {
    private var categoryAmounts = emptyList<CategoryAmount>()
    private var paints = mapOf<Category?, Paint>()
    private var totalAmount = 0.0

    val rectangle = RectF(
        0f,
        0f,
        com.ivy.ui.platform.convertDpToPixel(context, 2 * RADIUS_DP),
        com.ivy.ui.platform.convertDpToPixel(context, 2 * RADIUS_DP)
    )

    var onCategoryClicked: (Category?) -> Unit = {}

    fun display(
        categoryAmounts: List<CategoryAmount>,
        selectedCategory: SelectedCategory?,
        onCategoryClicked: (Category?) -> Unit
    ) {
        this.onCategoryClicked = onCategoryClicked

        this.categoryAmounts = categoryAmounts
        this.totalAmount = categoryAmounts.sumOf { it.amount }

        this.paints = categoryAmounts
            .map {
                val category = it.category
                val categoryColor = category?.color?.value?.toComposeColor() ?: Gray
                val color = if (selectedCategory == null) {
                    categoryColor
                } else {
                    if (selectedCategory.categoryId == category?.id?.value) {
                        categoryColor
                    } else {
                        categoryColor.copy(
                            alpha = 0.15f
                        )
                    }
                }

                category to paintFor(
                    color = color
                )
            }
            .toMap()

        invalidate()
    }

    private fun paintFor(color: Color): Paint {
        return Paint().apply {
            this.color = color.toArgb()
            this.strokeWidth = com.ivy.ui.platform.convertDpToPixel(context, 2f)
            this.strokeCap = Paint.Cap.ROUND
            this.strokeJoin = Paint.Join.ROUND
            this.isAntiAlias = true
        }
    }

    private val zones = mutableListOf<Zone>()

    override fun onDraw(canvas: Canvas) {
        var startAngle = -90.0

        zones.clear()

        for (categoryAmount in categoryAmounts) {
            val paint = paints[categoryAmount.category] ?: continue
            val amount = categoryAmount.amount

            if (amount == 0.0) continue

            val percent = amount / totalAmount
            val sweepAngle = 360 * percent

            zones.add(
                Zone(
                    startAngle = startAngle,
                    endAngle = startAngle + sweepAngle,
                    category = categoryAmount.category
                )
            )

            canvas.drawArc(
                rectangle,
                startAngle.toFloat(),
                sweepAngle.toFloat(),
                true,
                paint
            ) // draw

            startAngle += sweepAngle
        }
    }

    private var startClickTime = 0L

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startClickTime = SystemClock.elapsedRealtime()
            }

            MotionEvent.ACTION_UP -> {
                val clickDuration: Long =
                    SystemClock.elapsedRealtime() - startClickTime
                if (clickDuration < MAX_CLICK_DURATION) {
                    val touchX = event.x
                    val touchY = event.y

                    val centerX = width / 2f
                    val centerY = height / 2f

                    val angle = getAngle(
                        touchX = touchX,
                        touchY = touchY,
                        centerX = centerX,
                        centerY = centerY
                    )

                    val clickedCategory = zones
                        .firstOrNull { zone ->
                            zone.contains(angle = angle)
                        }?.category

                    onCategoryClicked(clickedCategory)
                }
            }
        }
        return true
    }

    private fun getAngle(
        touchX: Float,
        touchY: Float,
        centerX: Float,
        centerY: Float
    ): Double {
        val angle: Double
        val x2 = touchX - centerX
        val y2 = touchY - centerY
        val d1 = sqrt((centerY * centerY).toDouble())
        val d2 = sqrt((x2 * x2 + y2 * y2).toDouble())
        angle = if (touchX >= centerX) {
            Math.toDegrees(acos((-centerY * y2) / (d1 * d2)))
        } else {
            360 - Math.toDegrees(acos((-centerY * y2) / (d1 * d2)))
        }
        return angle - 90.0
    }

    companion object {
        private const val MAX_CLICK_DURATION = 200
    }

    private data class Zone(
        val startAngle: Double,
        val endAngle: Double,
        val category: Category?
    ) {
        fun contains(angle: Double): Boolean =
            angle > startAngle && angle < endAngle
    }
}
