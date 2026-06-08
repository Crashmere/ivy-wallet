package com.ivy.importdata.csvimport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivy.data.model.ExternalFile
import com.ivy.data.model.importing.ImportResult
import com.ivy.domain.usecase.backup.ImportBackupUseCase
import com.ivy.ui.platform.FilePicker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class ImportViewModel @Inject constructor(
    private val filePicker: FilePicker,
    private val importBackupUseCase: ImportBackupUseCase
) : ViewModel() {
    private val _importStep = MutableStateFlow(ImportStep.IMPORT_FROM)
    val importStep: StateFlow<ImportStep> = _importStep.asStateFlow()

    private val _importProgressPercent = MutableStateFlow(0)
    val importProgressPercent: StateFlow<Int> = _importProgressPercent.asStateFlow()

    private val _importResult = MutableStateFlow<ImportResult?>(null)
    val importResult: StateFlow<ImportResult?> = _importResult.asStateFlow()

    fun handleBack(): Boolean {
        return when (importStep.value) {
            ImportStep.IMPORT_FROM -> false
            ImportStep.LOADING -> {
                // do nothing, disable back
                true
            }

            ImportStep.RESULT -> {
                _importStep.value = ImportStep.IMPORT_FROM
                true
            }
        }
    }

    fun restoreBackup() {
        filePicker.openFile { fileUri ->
            viewModelScope.launch {

                _importStep.value = ImportStep.LOADING
                _importResult.value = importBackupUseCase(
                    backupFile = ExternalFile(fileUri.toString())
                ) { progressPercent ->
                    _importProgressPercent.value =
                        (progressPercent * 100).roundToInt()
                }
                _importStep.value = ImportStep.RESULT

            }
        }
    }

    fun skip() {
        resetState()
    }

    fun finish() {
        resetState()
    }

    private fun resetState() {
        _importStep.value = ImportStep.IMPORT_FROM
        _importProgressPercent.value = 0
        _importResult.value = null
    }
}
