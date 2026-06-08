package com.ivy.importdata.csvimport

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ivy.ui.navigation.screenScopedViewModel
import com.ivy.data.model.importing.ImportResult
import com.ivy.ui.navigation.CSVScreen
import com.ivy.importdata.csvimport.flow.ImportFrom
import com.ivy.importdata.csvimport.flow.ImportProcessing
import com.ivy.importdata.csvimport.flow.ImportResultUI
import com.ivy.ui.navigation.navigation

@ExperimentalFoundationApi
@Composable
fun BoxWithConstraintsScope.ImportCSVScreen() {
    val viewModel: ImportViewModel = screenScopedViewModel()
    val nav = navigation()

    val importStep by viewModel.importStep.collectAsState()
    val importProgressPercent by viewModel.importProgressPercent.collectAsState()
    val importResult by viewModel.importResult.collectAsState()

    BackHandler(enabled = importStep != ImportStep.IMPORT_FROM) {
        viewModel.handleInternalBack()
    }

    UI(
        importStep = importStep,
        importProgressPercent = importProgressPercent,
        importResult = importResult,

        onRestoreBackup = viewModel::restoreBackup,
        onSkip = {
            viewModel.skip()
            nav.back()
        },
        onFinish = {
            viewModel.finish()
            nav.back()
        },
        onBack = { nav.back() },
        onManualCsvImport = { nav.navigateTo(CSVScreen) },
    )
}

@ExperimentalFoundationApi
@Composable
private fun BoxWithConstraintsScope.UI(
    importStep: ImportStep,
    importProgressPercent: Int,
    importResult: ImportResult?,

    onRestoreBackup: () -> Unit = {},
    onSkip: () -> Unit = {},
    onFinish: () -> Unit = {},
    onBack: () -> Unit = {},
    onManualCsvImport: () -> Unit = {},
) {
    when (importStep) {
        ImportStep.IMPORT_FROM -> {
            ImportFrom(
                hasSkip = false,
                onBack = onBack,
                onSkip = onSkip,
                onRestoreBackup = onRestoreBackup,
                onManualCsvImport = onManualCsvImport
            )
        }

        ImportStep.LOADING -> {
            ImportProcessing(
                progressPercent = importProgressPercent
            )
        }

        ImportStep.RESULT -> {
            ImportResultUI(
                result = importResult!!,
                onBack = onFinish,
                onManualCsvImport = onManualCsvImport,
            ) {
                onFinish()
            }
        }
    }
}
