package com.ivy.legacy.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ivy.data.model.IntervalType
import com.ivy.legacy.ui.theme.system.LegacyTheme
import com.ivy.legacy.ui.theme.system.style
import com.ivy.base.legacy.capitalizeLocal
import com.ivy.base.legacy.isNotNullOrBlank
import com.ivy.ui.legacy.forDisplay
import com.ivy.ui.legacy.selectEndTextFieldValue
import com.ivy.ui.R
import com.ivy.legacy.ui.theme.Gradient
import com.ivy.legacy.ui.theme.GradientIvy
import com.ivy.legacy.ui.theme.White
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

private const val RepeatIntervalCharLimit = 5

@Deprecated("Old design system. Use `:ivy-design` and Material3")
@Composable
fun IntervalPickerRow(
    intervalN: Int,
    intervalType: IntervalType,

    onSetIntervalN: (Int) -> Unit,
    onSetIntervalType: (IntervalType) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(24.dp))

        var interNTextFieldValue by remember(intervalN) {
            mutableStateOf(selectEndTextFieldValue(intervalN.toString()))
        }

        val validInput = intervalN > 0 && interNTextFieldValue.text.isNotNullOrBlank()

        IvyNumberTextField(
            modifier = Modifier
                .background(
                    brush = if (validInput) {
                        GradientIvy.asHorizontalBrush()
                    } else {
                        Gradient
                            .solid(LegacyTheme.colors.medium)
                            .asHorizontalBrush()
                    },
                    shape = LegacyTheme.shapes.rFull
                )
                .padding(vertical = 12.dp),
            value = interNTextFieldValue,
            textColor = if (validInput) White else LegacyTheme.colors.pureInverse,
            hint = "0"
        ) {
            val filteredText = it.text.take(RepeatIntervalCharLimit)
            if (it.text != interNTextFieldValue.text) {
                try {
                    onSetIntervalN(filteredText.toInt())
                } catch (e: Exception) {
                }
            }
            interNTextFieldValue = it.copy(text = filteredText)
        }

        Spacer(Modifier.width(12.dp))

        IntervalTypeSelector(
            intervalN = intervalN,
            intervalType = intervalType
        ) {
            onSetIntervalType(it)
        }

        Spacer(Modifier.width(24.dp))
    }
}

@Composable
private fun RowScope.IntervalTypeSelector(
    intervalN: Int,
    intervalType: IntervalType,

    onSetIntervalType: (IntervalType) -> Unit
) {
    Row(
        modifier = Modifier
            .weight(1f)
            .border(2.dp, LegacyTheme.colors.medium, LegacyTheme.shapes.rFull),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(20.dp))

        IvyIcon(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable {
                    onSetIntervalType(
                        when (intervalType) {
                            IntervalType.DAY -> IntervalType.YEAR
                            IntervalType.WEEK -> IntervalType.DAY
                            IntervalType.MONTH -> IntervalType.WEEK
                            IntervalType.YEAR -> IntervalType.MONTH
                        }
                    )
                }
                .padding(all = 8.dp)
                .rotate(-180f),
            icon = R.drawable.ic_arrow_right,
            contentDescription = "interval_type_arrow_left"
        )

        Spacer(Modifier.weight(1f))

        Text(
            text = intervalType.forDisplay(intervalN).capitalizeLocal(),
            style = LegacyTheme.typo.b2.style(
                color = LegacyTheme.colors.pureInverse,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(Modifier.weight(1f))

        IvyIcon(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable {
                    onSetIntervalType(
                        when (intervalType) {
                            IntervalType.DAY -> IntervalType.WEEK
                            IntervalType.WEEK -> IntervalType.MONTH
                            IntervalType.MONTH -> IntervalType.YEAR
                            IntervalType.YEAR -> IntervalType.DAY
                        }
                    )
                }
                .padding(all = 8.dp),
            icon = R.drawable.ic_arrow_right,
            contentDescription = "interval_type_arrow_right"
        )

        Spacer(Modifier.width(20.dp))
    }
}
