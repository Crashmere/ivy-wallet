package com.ivy.legacy.ui.modal

import android.annotation.SuppressLint
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.data.model.IntervalType
import com.ivy.ui.time.LocalTimeConverter
import com.ivy.ui.time.LocalTimeProvider
import com.ivy.legacy.ui.theme.LegacyTheme
import com.ivy.data.model.FromToTimeRange
import com.ivy.ui.period.LastNTimeRange
import com.ivy.ui.period.Month.Companion.fromMonthValue
import com.ivy.ui.period.Month.Companion.monthsList
import com.ivy.ui.period.TimePeriod
import com.ivy.ui.period.displayName
import com.ivy.ui.platform.addKeyboardListener
import com.ivy.ui.time.formatDateOnlyWithYear
import com.ivy.ui.compose.onCompositionStart
import com.ivy.ui.R
import com.ivy.legacy.ui.theme.Gradient
import com.ivy.legacy.ui.theme.GradientIvy
import com.ivy.legacy.ui.theme.Gray
import com.ivy.legacy.ui.theme.Green
import com.ivy.legacy.ui.theme.White
import com.ivy.ui.compose.FilledIconButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import java.time.LocalDate
import java.util.UUID

@SuppressLint("ComposeModifierMissing")
@Suppress("ParameterNaming")
@Composable
fun BoxWithConstraintsScope.ChoosePeriodModal(
    modal: TimePeriod?,

    dismiss: () -> Unit,
    saveSelectedPeriod: (TimePeriod) -> Unit,
    pickDate: (
        minDate: LocalDate?,
        maxDate: LocalDate?,
        initialDate: LocalDate?,
        onDatePicked: (LocalDate) -> Unit
    ) -> Unit,
    onPeriodSelected: (TimePeriod) -> Unit
) {
    var period by remember(modal) {
        mutableStateOf(modal)
    }
    val modalId = remember(modal) {
        modal?.let { UUID.randomUUID() }
    }

    val modalScrollState = rememberScrollState()
    val currentDate = LocalTimeProvider.current.localDateNow()

    IvyModal(
        id = modalId,
        visible = modal != null,
        dismiss = dismiss,
        scrollState = modalScrollState,
        PrimaryAction = {
            ModalSet(
                enabled = period != null && period!!.isValid()
            ) {
                if (period != null) {
                    saveSelectedPeriod(period!!)
                    dismiss()
                    onPeriodSelected(period!!)
                }
            }
        }
    ) {
        Spacer(Modifier.height(32.dp))

        ChooseMonth(
            selectedMonthYear = period?.month?.let {
                MonthYear(month = it, year = period?.year ?: currentDate.year)
            }
        ) {
            period = TimePeriod(
                month = it.month,
                year = it.year
            )
        }

        Spacer(Modifier.height(32.dp))

        IvyDividerLine(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )

        Spacer(Modifier.height(32.dp))

        FromToRange(
            timeRange = period?.fromToRange,
            pickDate = pickDate,
        ) {
            period = TimePeriod(
                fromToRange = it
            )
        }

        Spacer(Modifier.height(32.dp))

        LastNPeriod(
            modalScrollState = modalScrollState,
            lastNTimeRange = period?.lastNRange,
        ) {
            period = TimePeriod(
                lastNRange = it
            )
        }

        Spacer(Modifier.height(32.dp))

        AllTime(
            timeRange = period?.fromToRange
        ) {
            period = TimePeriod(
                fromToRange = it
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
@Suppress("ParameterNaming")
private fun ColumnScope.ChooseMonth(
    selectedMonthYear: MonthYear?,
    onSelected: (MonthYear) -> Unit,
) {
    Text(
        modifier = Modifier
            .padding(start = 32.dp),
        text = stringResource(R.string.choose_month),
        style = LegacyTheme.typo.b1.copy(
            color = if (selectedMonthYear != null) LegacyTheme.colors.pureInverse else Gray,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Start
        )
    )

    Spacer(Modifier.height(24.dp))

    val currentDate = LocalTimeProvider.current.localDateNow()
    val currentYear = currentDate.year
    val months = remember(currentYear) {
        monthsList()
            .map {
                MonthYear(month = it, year = currentYear - 1)
            }
            .plus(
                monthsList().map { MonthYear(month = it, year = currentYear) }
            )
            .plus(
                monthsList().map { MonthYear(month = it, year = currentYear + 1) }
            )
    }

    val state = rememberLazyListState()

    val coroutineScope = rememberCoroutineScope()
    onCompositionStart {
        if (selectedMonthYear != null) {
            val selectedMonthIndex = months.indexOf(selectedMonthYear)
            if (selectedMonthIndex != -1) {
                coroutineScope.launch {
                    state.scrollToItem(selectedMonthIndex)
                }
            }
        } else {
            val currentMonthYear = MonthYear(
                month = fromMonthValue(currentDate.monthValue),
                year = currentYear
            )
            val currentMonthIndex = months.indexOf(currentMonthYear)
            if (currentMonthIndex != -1) {
                coroutineScope.launch {
                    state.scrollToItem(currentMonthIndex)
                }
            }
        }
    }

    LazyRow(
        state = state,
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            Spacer(Modifier.width(12.dp))
        }

        items(items = months) { monthYear ->
            MonthButton(
                selected = monthYear == selectedMonthYear,
                text = monthYear.forDisplay(currentYear = currentYear)
            ) {
                onSelected(monthYear)
            }

            Spacer(Modifier.width(12.dp))
        }
    }
}

private data class MonthYear(
    val month: com.ivy.ui.period.Month,
    val year: Int
) {
    @Composable
    fun forDisplay(
        currentYear: Int
    ): String {
        val monthName = month.displayName()
        return if (year != currentYear) {
            // not current year
            "$monthName, $year"
        } else {
            // current year
            monthName
        }
    }
}

@Composable
private fun MonthButton(
    selected: Boolean,
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val background = if (selected) GradientIvy else Gradient.solid(LegacyTheme.colors.medium)
    Text(
        modifier = modifier
            .clip(LegacyTheme.shapes.rFull)
            .background(
                brush = background.asHorizontalBrush(),
                shape = LegacyTheme.shapes.rFull
            )
            .clickable {
                onClick()
            }
            .padding(horizontal = 24.dp)
            .padding(
                vertical = 12.dp,
            ),
        text = text,
        style = LegacyTheme.typo.b2.copy(
            fontWeight = FontWeight.Bold,
            color = if (selected) White else Gray,
            textAlign = TextAlign.Start
        )
    )
}

@Composable
@Suppress("ParameterNaming")
private fun ColumnScope.FromToRange(
    timeRange: FromToTimeRange?,
    pickDate: (
        minDate: LocalDate?,
        maxDate: LocalDate?,
        initialDate: LocalDate?,
        onDatePicked: (LocalDate) -> Unit
    ) -> Unit,
    onSelected: (FromToTimeRange?) -> Unit,
) {
    Text(
        modifier = Modifier
            .padding(start = 32.dp),
        text = stringResource(R.string.or_custom_range),
        style = LegacyTheme.typo.b1.copy(
            color = if (timeRange != null) LegacyTheme.colors.pureInverse else Gray,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Start
        )
    )

    Spacer(Modifier.height(16.dp))

    val converter = LocalTimeConverter.current
    IntervalFromToDate(
        border = IntervalBorder.FROM,
        dateTime = with(converter) { timeRange?.from?.toLocalDateTime() },
        otherEndDateTime = with(converter) { timeRange?.to?.toLocalDateTime() },
        pickDate = pickDate,
    ) { from ->
        onSelected(
            if (from == null && timeRange?.to == null) {
                null
            } else {
                timeRange?.copy(
                    from = with(converter) { from?.toUTC() }
                ) ?: FromToTimeRange(
                    from = with(converter) { from?.toUTC() },
                    to = null
                )
            }
        )
    }

    Spacer(Modifier.height(12.dp))

    IntervalFromToDate(
        border = IntervalBorder.TO,
        dateTime = with(converter) { timeRange?.to?.toLocalDateTime() },
        otherEndDateTime = with(converter) { timeRange?.from?.toLocalDateTime() },
        pickDate = pickDate,
    ) { to ->
        onSelected(
            if (timeRange?.from == null && to == null) {
                null
            } else {
                timeRange?.copy(
                    to = with(converter) { to?.plusDays(1)?.minusNanos(1)?.toUTC() }
                ) ?: FromToTimeRange(
                    from = null,
                    to = with(converter) { to?.toUTC() }
                )
            }
        )
    }
}

@Composable
@Suppress("ParameterNaming")
private fun IntervalFromToDate(
    border: IntervalBorder,
    dateTime: LocalDateTime?,
    otherEndDateTime: LocalDateTime?,
    pickDate: (
        minDate: LocalDate?,
        maxDate: LocalDate?,
        initialDate: LocalDate?,
        onDatePicked: (LocalDate) -> Unit
    ) -> Unit,
    onSelected: (LocalDateTime?) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .clip(LegacyTheme.shapes.rFull)
            .border(2.dp, LegacyTheme.colors.medium, LegacyTheme.shapes.rFull)
            .clickable {
                pickDate(
                    if (border == IntervalBorder.TO) {
                        otherEndDateTime
                            ?.toLocalDate()
                            ?.plusDays(1)
                    } else {
                        null
                    },
                    if (border == IntervalBorder.FROM) {
                        otherEndDateTime
                            ?.toLocalDate()
                            ?.minusDays(1)
                    } else {
                        null
                    },
                    dateTime?.toLocalDate()
                ) {
                    onSelected(it.atStartOfDay())
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(32.dp))

        Text(
            modifier = Modifier
                .padding(
                    vertical = 16.dp,
                ),
            text = if (border == IntervalBorder.FROM) {
                stringResource(R.string.from)
            } else {
                stringResource(
                    R.string.to
                )
            },
            style = LegacyTheme.typo.b2.copy(
                fontWeight = FontWeight.ExtraBold,
                color = if (dateTime != null) Green else LegacyTheme.colors.pureInverse,
                textAlign = TextAlign.Start
            )
        )

        if (dateTime != null) {
            Spacer(Modifier.width(16.dp))
        } else {
            Spacer(Modifier.weight(1f))
        }

        Text(
            text = dateTime?.toLocalDate()?.formatDateOnlyWithYear()
                ?: stringResource(R.string.add_date),
            style = LegacyTheme.typo.nB2.copy(
                fontWeight = FontWeight.Bold,
                color = if (dateTime != null) LegacyTheme.colors.pureInverse else Gray,
                textAlign = TextAlign.Start
            )
        )

        if (dateTime != null) {
            Spacer(Modifier.weight(1f))

            FilledIconButton(
                icon = R.drawable.ic_dismiss,
                backgroundColor = LegacyTheme.colors.medium,
                tint = LegacyTheme.colors.pureInverse,
            ) {
                onSelected(null)
            }

            Spacer(Modifier.width(4.dp))
        } else {
            Spacer(Modifier.width(32.dp))
        }
    }
}

private enum class IntervalBorder {
    FROM, TO
}

@Composable
@Suppress("ParameterNaming")
private fun ColumnScope.LastNPeriod(
    modalScrollState: ScrollState,
    lastNTimeRange: LastNTimeRange?,

    onSelected: (LastNTimeRange) -> Unit
) {
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

    Text(
        modifier = Modifier
            .padding(start = 32.dp),
        text = stringResource(R.string.or_in_the_last),
        style = LegacyTheme.typo.b1.copy(
            color = if (lastNTimeRange != null) LegacyTheme.colors.pureInverse else Gray,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Start
        )
    )

    Spacer(Modifier.height(16.dp))

    IntervalPickerRow(
        intervalN = lastNTimeRange?.periodN ?: 0,
        intervalType = lastNTimeRange?.periodType ?: IntervalType.WEEK,
        onSetIntervalN = {
            onSelected(
                lastNTimeRange?.copy(
                    periodN = it
                ) ?: com.ivy.ui.period.LastNTimeRange(
                    periodN = it,
                    periodType = IntervalType.WEEK
                )
            )
        },
        onSetIntervalType = {
            onSelected(
                lastNTimeRange?.copy(
                    periodType = it
                ) ?: com.ivy.ui.period.LastNTimeRange(
                    periodN = 1,
                    periodType = it
                )
            )
        }
    )
}

@Composable
@Suppress("ParameterNaming", "MagicNumber")
private fun ColumnScope.AllTime(
    timeRange: FromToTimeRange?,
    onSelected: (FromToTimeRange?) -> Unit,
) {
    val timeProvider = LocalTimeProvider.current
    val active = timeRange != null && timeRange.from == null &&
            timeRange.to != null && timeRange.to!!.isAfter(timeProvider.utcNow())

    Text(
        modifier = Modifier
            .padding(start = 32.dp),
        text = stringResource(R.string.or_all_time),
        style = LegacyTheme.typo.b1.copy(
            color = if (active) LegacyTheme.colors.pureInverse else Gray,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Start
        )
    )

    Spacer(Modifier.height(16.dp))

    MonthButton(
        modifier = Modifier.padding(start = 32.dp),
        selected = active,
        text = if (active) stringResource(R.string.unselect_all_time) else stringResource(R.string.select_all_time)
    ) {
        onSelected(
            if (active) {
                null
            } else {
                FromToTimeRange(
                    from = null,
                    to = timeProvider.utcNow().plusSeconds(TimeUnit.HOURS.toSeconds(24L))
                )
            }
        )
    }
}
