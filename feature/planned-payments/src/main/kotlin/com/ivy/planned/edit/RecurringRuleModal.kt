package com.ivy.planned.edit

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.data.model.IntervalType
import com.ivy.ui.compose.GradientButton
import com.ivy.ui.compose.GradientIconButton
import com.ivy.ui.compose.ResourceIcon
import com.ivy.legacy.ui.modal.IvyModal
import com.ivy.legacy.ui.modal.ModalTitle
import com.ivy.ui.theme.colors.Gradient
import com.ivy.ui.theme.colors.IvyGradients
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.ui.theme.colors.IvyFixedColors.White
import com.ivy.ui.R
import com.ivy.ui.compose.clickableNoIndication
import com.ivy.ui.compose.onCompositionStart
import com.ivy.ui.compose.rememberInteractionSource
import com.ivy.ui.compose.selectEndTextFieldValue
import com.ivy.ui.compose.thenIf
import com.ivy.ui.platform.addKeyboardListener
import com.ivy.ui.platform.hideKeyboard
import com.ivy.ui.time.LocalTimeProvider
import com.ivy.ui.time.closeDay
import com.ivy.ui.time.forDisplay
import com.ivy.ui.time.formatDateWeekDayLong
import com.ivy.ui.time.formatNicely
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale
import java.util.UUID

private const val RepeatIntervalCharLimit = 5

@Suppress("ParameterNaming")
@Composable
internal fun BoxWithConstraintsScope.RecurringRuleModal(
    visible: Boolean,
    initialStartDate: LocalDateTime?,
    initialIntervalN: Int?,
    initialIntervalType: IntervalType?,
    initialOneTime: Boolean,
    pickDate: (LocalDate, (LocalDateTime) -> Unit) -> Unit,
    dismiss: () -> Unit,
    onRuleChanged: (LocalDateTime, oneTime: Boolean, Int?, IntervalType?) -> Unit,
) {
    val timeProvider = LocalTimeProvider.current
    val modalId = remember(visible) {
        UUID.randomUUID()
    }
    var startDate by remember(visible, initialStartDate) {
        mutableStateOf(initialStartDate ?: timeProvider.localNow())
    }
    var oneTime by remember(visible, initialOneTime) {
        mutableStateOf(initialOneTime)
    }
    var intervalN by remember(visible, initialIntervalN) {
        mutableStateOf(initialIntervalN ?: 1)
    }
    var intervalType by remember(visible, initialIntervalType) {
        mutableStateOf(initialIntervalType ?: IntervalType.MONTH)
    }

    val modalScrollState = rememberScrollState()

    IvyModal(
        id = modalId,
        visible = visible,
        dismiss = dismiss,
        scrollState = modalScrollState,
        PrimaryAction = {
            RecurringRuleSetButton(
                modifier = Modifier.testTag("recurringModalSet"),
                enabled = validate(oneTime, intervalN, intervalType)
            ) {
                dismiss()
                onRuleChanged(
                    startDate,
                    oneTime,
                    intervalN,
                    intervalType
                )
            }
        }
    ) {
        Spacer(Modifier.height(32.dp))

        val rootView = LocalView.current
        onCompositionStart {
            rootView.hideKeyboard()
        }

        ModalTitle(text = stringResource(R.string.plan_for))

        Spacer(Modifier.height(16.dp))

        TimesSelector(oneTime = oneTime) {
            oneTime = it
        }

        if (oneTime) {
            OneTime(
                date = startDate,
                pickDate = pickDate,
                onDatePicked = {
                    startDate = it
                }
            )
        } else {
            MultipleTimes(
                startDate = startDate,
                intervalN = intervalN,
                intervalType = intervalType,
                modalScrollState = modalScrollState,
                pickDate = pickDate,
                onSetStartDate = {
                    startDate = it
                },
                onSetIntervalN = {
                    intervalN = it
                },
                onSetIntervalType = {
                    intervalType = it
                }
            )
        }
    }
}

private fun validate(
    oneTime: Boolean,
    intervalN: Int?,
    intervalType: IntervalType?
): Boolean {
    return oneTime || intervalN != null && intervalN > 0 && intervalType != null
}

@Composable
private fun RecurringRuleSetButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    GradientButton(
        modifier = modifier,
        text = stringResource(R.string.set),
        backgroundGradient = IvyGradients.Green,
        disabledBackgroundColor = LegacyTheme.colors.gray,
        shape = LegacyTheme.shapes.rFull,
        textStyle = LegacyTheme.typo.b2.copy(
            color = Color(0xFFFAFAFA),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start
        ),
        iconTint = Color(0xFFFAFAFA),
        iconStart = R.drawable.ic_check,
        enabled = enabled,
        onClick = onClick
    )
}

@Composable
private fun TimesSelector(
    oneTime: Boolean,
    onSetOneTime: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .background(LegacyTheme.colors.medium, LegacyTheme.shapes.r2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(8.dp))

        TimesSelectorButton(
            selected = oneTime,
            label = stringResource(R.string.one_time)
        ) {
            onSetOneTime(true)
        }

        Spacer(Modifier.width(8.dp))

        TimesSelectorButton(
            selected = !oneTime,
            label = stringResource(R.string.multiple_times)
        ) {
            onSetOneTime(false)
        }

        Spacer(Modifier.width(8.dp))
    }
}

@Composable
private fun RowScope.TimesSelectorButton(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    val rFull = LegacyTheme.shapes.rFull

    Text(
        modifier = Modifier
            .weight(1f)
            .clip(LegacyTheme.shapes.rFull)
            .clickable {
                onClick()
            }
            .padding(vertical = 8.dp)
            .thenIf(selected) {
                background(IvyGradients.Ivy.asHorizontalBrush(), rFull)
            }
            .padding(vertical = 8.dp),
        text = label,
        style = LegacyTheme.typo.b2.copy(
            color = if (selected) White else LegacyTheme.colors.gray,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
    )
}

@Suppress("ParameterNaming")
@Composable
private fun OneTime(
    date: LocalDateTime,
    pickDate: (LocalDate, (LocalDateTime) -> Unit) -> Unit,
    onDatePicked: (LocalDateTime) -> Unit
) {
    Spacer(Modifier.height(44.dp))

    DateRow(
        dateTime = date,
        pickDate = pickDate
    ) {
        onDatePicked(it)
    }

    Spacer(Modifier.height(64.dp))
}

@Composable
private fun MultipleTimes(
    startDate: LocalDateTime,
    intervalN: Int,
    intervalType: IntervalType,
    modalScrollState: ScrollState,
    pickDate: (LocalDate, (LocalDateTime) -> Unit) -> Unit,
    onSetStartDate: (LocalDateTime) -> Unit,
    onSetIntervalN: (Int) -> Unit,
    onSetIntervalType: (IntervalType) -> Unit
) {
    Spacer(Modifier.height(40.dp))

    Text(
        modifier = Modifier.padding(start = 32.dp),
        text = stringResource(R.string.starts_on),
        style = LegacyTheme.typo.b2.copy(
            color = LegacyTheme.colors.pureInverse,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Start
        )
    )

    Spacer(Modifier.height(12.dp))

    DateRow(
        dateTime = startDate,
        pickDate = pickDate
    ) {
        onSetStartDate(it)
    }

    Spacer(Modifier.height(32.dp))

    RecurringRuleDividerLine(
        modifier = Modifier.padding(horizontal = 24.dp)
    )

    Spacer(Modifier.height(32.dp))

    Text(
        modifier = Modifier.padding(start = 32.dp),
        text = stringResource(R.string.repeats_every_text),
        style = LegacyTheme.typo.b2.copy(
            fontWeight = FontWeight.ExtraBold,
            color = LegacyTheme.colors.pureInverse,
            textAlign = TextAlign.Start
        )
    )

    Spacer(Modifier.height(16.dp))

    val rootView = LocalView.current
    val coroutineScope = rememberCoroutineScope()

    onCompositionStart {
        rootView.addKeyboardListener { keyboardShown ->
            if (keyboardShown) {
                coroutineScope.launch {
                    delay(200)
                    modalScrollState.animateScrollTo(modalScrollState.maxValue)
                }
            }
        }
    }

    RepeatIntervalPickerRow(
        intervalN = intervalN,
        intervalType = intervalType,
        onSetIntervalN = onSetIntervalN,
        onSetIntervalType = onSetIntervalType
    )

    Spacer(Modifier.height(48.dp))
}

@Composable
private fun RecurringRuleDividerLine(
    modifier: Modifier = Modifier,
) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(LegacyTheme.colors.medium)
    )
}

@Composable
@Suppress("ParameterNaming")
private fun DateRow(
    dateTime: LocalDateTime,
    pickDate: (LocalDate, (LocalDateTime) -> Unit) -> Unit,
    onDatePicked: (LocalDateTime) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(32.dp))

        Column(
            modifier = Modifier.clickableNoIndication(rememberInteractionSource()) {
                pickDate(dateTime.toLocalDate(), onDatePicked)
            }
        ) {
            val date = dateTime.toLocalDate()
            val closeDay = date.closeDay()

            Text(
                text = closeDay ?: date.formatNicely(
                    pattern = "EEEE, dd MMM"
                ),
                style = LegacyTheme.typo.h2.copy(
                    fontWeight = FontWeight.Normal,
                    color = LegacyTheme.colors.pureInverse,
                    textAlign = TextAlign.Start
                )
            )

            if (closeDay != null) {
                Spacer(Modifier.height(4.dp))

                Text(
                    text = date.formatDateWeekDayLong(),
                    style = LegacyTheme.typo.b2.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = LegacyTheme.colors.gray,
                        textAlign = TextAlign.Start
                    )
                )
            }
        }

        Spacer(Modifier.width(24.dp))
        Spacer(Modifier.weight(1f))

        GradientIconButton(
            modifier = Modifier
                .size(48.dp)
                .testTag("recurring_modal_pick_date"),
            backgroundPadding = 4.dp,
            icon = R.drawable.ic_calendar,
            backgroundGradient = Gradient.solid(LegacyTheme.colors.pureInverse),
            tint = LegacyTheme.colors.pure
        ) {
            pickDate(dateTime.toLocalDate(), onDatePicked)
        }

        Spacer(Modifier.width(32.dp))
    }
}

@Composable
private fun RepeatIntervalPickerRow(
    intervalN: Int,
    intervalType: IntervalType,
    onSetIntervalN: (Int) -> Unit,
    onSetIntervalType: (IntervalType) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(24.dp))

        var intervalTextFieldValue by remember(intervalN) {
            mutableStateOf(selectEndTextFieldValue(intervalN.toString()))
        }

        val validInput = intervalN > 0 && intervalTextFieldValue.text.isNotBlank()

        RepeatIntervalNumberInput(
            modifier = Modifier
                .background(
                    brush = if (validInput) {
                        IvyGradients.Ivy.asHorizontalBrush()
                    } else {
                        Gradient.solid(LegacyTheme.colors.medium).asHorizontalBrush()
                    },
                    shape = LegacyTheme.shapes.rFull
                )
                .padding(vertical = 12.dp),
            value = intervalTextFieldValue,
            textColor = if (validInput) White else LegacyTheme.colors.pureInverse,
            hint = "0"
        ) {
            val filteredText = it.text.take(RepeatIntervalCharLimit)
            if (it.text != intervalTextFieldValue.text) {
                filteredText.toIntOrNull()?.let(onSetIntervalN)
            }
            intervalTextFieldValue = it.copy(text = filteredText)
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
private fun RepeatIntervalNumberInput(
    modifier: Modifier = Modifier,
    value: TextFieldValue,
    textColor: androidx.compose.ui.graphics.Color,
    hint: String,
    onValueChanged: (TextFieldValue) -> Unit
) {
    val isEmpty = value.text.isBlank()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (isEmpty) {
            Text(
                text = hint,
                textAlign = TextAlign.Start,
                style = LegacyTheme.typo.nB2.copy(
                    color = androidx.compose.ui.graphics.Color.Gray,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            )
        }

        val view = LocalView.current
        BasicTextField(
            modifier = Modifier.testTag("base_number_input"),
            value = value,
            onValueChange = onValueChanged,
            textStyle = LegacyTheme.typo.nB2.copy(
                color = textColor,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            ),
            singleLine = true,
            cursorBrush = SolidColor(LegacyTheme.colors.pureInverse),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Number,
                autoCorrect = false
            ),
            keyboardActions = KeyboardActions(
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
