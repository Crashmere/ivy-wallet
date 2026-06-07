package com.ivy.piechart

import com.ivy.data.model.Category
import com.ivy.legacy.ui.model.period.TimePeriod
import com.ivy.ui.navigation.PieChartStatisticScreen

sealed interface PieChartStatisticEvent {
    data class OnStart(val screen: PieChartStatisticScreen) : PieChartStatisticEvent
    data object OnSelectNextMonth : PieChartStatisticEvent
    data object OnSelectPreviousMonth : PieChartStatisticEvent
    data class OnSetPeriod(val timePeriod: TimePeriod) : PieChartStatisticEvent
    data class OnCategoryClicked(val category: Category?) : PieChartStatisticEvent
    data class OnShowMonthModal(val timePeriod: TimePeriod?) : PieChartStatisticEvent
}
