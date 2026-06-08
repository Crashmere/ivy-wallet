package com.ivy.importdata.csvimport

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ivy.data.model.importing.ImportResult
import com.ivy.importdata.csvimport.flow.ImportFrom
import com.ivy.importdata.csvimport.flow.ImportProcessing
import com.ivy.importdata.csvimport.flow.ImportResultUI
import com.ivy.ui.navigation.ImportScreen
import com.ivy.ui.navigation.onScreenStart

@ExperimentalFoundationApi
@Composable
fun BoxWithConstraintsScope.ImportCSVScreen() {
    val viewModel: ImportViewModel = viewModel()

    val importStep by viewModel.importStep.collectAsState()
    val importProgressPercent by viewModel.importProgressPercent.collectAsState()
    val importResult by viewModel.importResult.collectAsState()

    onScreenStart {
        viewModel.start(ImportScreen)
    }

    UI(
        importStep = importStep,
        importProgressPercent = importProgressPercent,
        importResult = importResult,

        onRestoreBackup = viewModel::restoreBackup,
        onSkip = viewModel::skip,
        onFinish = viewModel::finish,
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
) {
    when (importStep) {
        ImportStep.IMPORT_FROM -> {
            ImportFrom(
                hasSkip = false,
                onSkip = onSkip,
                onRestoreBackup = onRestoreBackup
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
            ) {
                onFinish()
            }
        }
    }
}
