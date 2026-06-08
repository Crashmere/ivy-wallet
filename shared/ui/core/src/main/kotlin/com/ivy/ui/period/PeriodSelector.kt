package com.ivy.ui.period

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.ui.R
import com.ivy.ui.compose.ResourceIcon
import com.ivy.ui.time.LocalTimeConverter
import com.ivy.ui.time.LocalTimeFormatter
import com.ivy.ui.time.LocalTimeProvider

@Composable
fun PeriodSelector(
    period: TimePeriod,
    startDateOfMonth: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onShowChoosePeriodModal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(2.dp, PeriodSelectorTheme.colors.medium, PeriodSelectorTheme.shapes.rFull),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.width(20.dp))

        if (period.month != null) {
            ResourceIcon(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable {
                        onPreviousMonth()
                    }
                    .padding(all = 8.dp)
                    .rotate(-180f),
                icon = R.drawable.ic_arrow_right,
                tint = PeriodSelectorTheme.colors.pureInverse,
            )
        }

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier
                .height(48.dp)
                .defaultMinSize(minWidth = 48.dp)
                .clip(PeriodSelectorTheme.shapes.rFull)
                .clickable {
                    onShowChoosePeriodModal()
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            ResourceIcon(
                icon = R.drawable.ic_calendar,
                tint = PeriodSelectorTheme.colors.pureInverse,
            )

            Spacer(Modifier.width(4.dp))

            Text(
                text = period.toDisplayShort(
                    startDateOfMonth = startDateOfMonth,
                    timeConverter = LocalTimeConverter.current,
                    timeProvider = LocalTimeProvider.current,
                    timeFormatter = LocalTimeFormatter.current,
                ),
                style = PeriodSelectorTheme.typo.b2.copy(
                    color = PeriodSelectorTheme.colors.pureInverse,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                ),
            )
        }

        Spacer(Modifier.weight(1f))

        if (period.month != null) {
            ResourceIcon(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable {
                        onNextMonth()
                    }
                    .padding(all = 8.dp),
                icon = R.drawable.ic_arrow_right,
                tint = PeriodSelectorTheme.colors.pureInverse,
            )
        }

        Spacer(Modifier.width(20.dp))
    }
}
