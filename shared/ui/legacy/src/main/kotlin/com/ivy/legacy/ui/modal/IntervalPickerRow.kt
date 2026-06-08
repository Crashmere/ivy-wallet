package com.ivy.legacy.ui.modal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.data.model.IntervalType
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.ui.time.forDisplay
import com.ivy.ui.compose.ResourceIcon
import com.ivy.ui.compose.selectEndTextFieldValue
import com.ivy.ui.R
import com.ivy.legacy.ui.theme.Gradient
import com.ivy.legacy.ui.theme.GradientIvy
import com.ivy.legacy.ui.theme.White
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.ivy.ui.platform.hideKeyboard
import java.util.Locale

private const val RepeatIntervalCharLimit = 5

@Composable
internal fun IntervalPickerRow(
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

        val validInput = intervalN > 0 && interNTextFieldValue.text.isNullOrBlank().not()

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
private fun IvyNumberTextField(
    modifier: Modifier = Modifier,
    textModifier: Modifier = Modifier,
    value: TextFieldValue,
    hint: String?,
    fontWeight: FontWeight = FontWeight.ExtraBold,
    textColor: Color = LegacyTheme.colors.pureInverse,
    hintColor: Color = Color.Gray,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions? = null,
    keyboardActions: KeyboardActions? = null,
    onValueChanged: (TextFieldValue) -> Unit
) {
    val isEmpty = value.text.isBlank()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (isEmpty && hint.isNullOrBlank().not()) {
            Text(
                modifier = textModifier,
                text = hint!!,
                textAlign = TextAlign.Start,
                style = LegacyTheme.typo.nB2.copy(
                    color = hintColor,
                    fontWeight = fontWeight,
                    textAlign = TextAlign.Center
                )
            )
        }

        val view = LocalView.current
        BasicTextField(
            modifier = textModifier
                .testTag("base_number_input"),
            value = value,
            onValueChange = onValueChanged,
            textStyle = LegacyTheme.typo.nB2.copy(
                color = textColor,
                fontWeight = fontWeight,
                textAlign = TextAlign.Center
            ),
            singleLine = true,
            cursorBrush = SolidColor(LegacyTheme.colors.pureInverse),
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions ?: KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Number,
                autoCorrect = false
            ),
            keyboardActions = keyboardActions ?: KeyboardActions(
                onDone = {
                    view.hideKeyboard()
                }
            )
        )
    }
}

private fun String.capitalizeLocal(): String = replaceFirstChar {
    if (it.isLowerCase()) {
        it.titlecase(Locale.getDefault())
    } else {
        it.toString()
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

        ResourceIcon(
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
            tint = LegacyTheme.colors.pureInverse,
            contentDescription = "interval_type_arrow_left"
        )

        Spacer(Modifier.weight(1f))

        Text(
            text = intervalType.forDisplay(intervalN).capitalizeLocal(),
            style = LegacyTheme.typo.b2.copy(
                color = LegacyTheme.colors.pureInverse,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start
            )
        )

        Spacer(Modifier.weight(1f))

        ResourceIcon(
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
            tint = LegacyTheme.colors.pureInverse,
            contentDescription = "interval_type_arrow_right"
        )

        Spacer(Modifier.width(20.dp))
    }
}
