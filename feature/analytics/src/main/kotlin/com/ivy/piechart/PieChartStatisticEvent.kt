package com.ivy.piechart

import com.ivy.ui.period.TimePeriod
import java.util.UUID

internal sealed interface PieChartStatisticEvent {
    data object OnSelectNextMonth : PieChartStatisticEvent
    data object OnSelectPreviousMonth : PieChartStatisticEvent
    data class OnSetPeriod(val timePeriod: TimePeriod) : PieChartStatisticEvent
    data class OnCategoryClicked(val categoryId: UUID?) : PieChartStatisticEvent
    data class OnGroupingSelected(val grouping: PieChartGrouping) : PieChartStatisticEvent
}
