package com.ivy.importdata.csvimport

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivy.data.backup.BackupDataUseCase
import com.ivy.data.backup.ImportResult
import com.ivy.base.lifecycle.asLiveData
import com.ivy.ui.navigation.ImportScreen
import com.ivy.ui.navigation.Navigation
import com.ivy.ui.platform.FilePicker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class ImportViewModel @Inject constructor(
    private val filePicker: FilePicker,
    private val nav: Navigation,
    private val backupDataUseCase: BackupDataUseCase
) : ViewModel() {
    private val _importStep = MutableLiveData<ImportStep>()
    val importStep = _importStep.asLiveData()

    private val _importProgressPercent = MutableLiveData<Int>()
    val importProgressPercent = _importProgressPercent.asLiveData()

    private val _importResult = MutableLiveData<ImportResult>()
    val importResult = _importResult.asLiveData()

    fun start(screen: ImportScreen) {
        nav.onBackPressed[screen] = {
            when (importStep.value) {
                ImportStep.IMPORT_FROM -> false
                ImportStep.LOADING -> {
                    // do nothing, disable back
                    true
                }

                ImportStep.RESULT -> {
                    _importStep.value = ImportStep.IMPORT_FROM
                    true
                }

                null -> false
            }
        }
    }

    fun restoreBackup() {
        filePicker.openFile { fileUri ->
            viewModelScope.launch {

                _importStep.value = ImportStep.LOADING
                _importResult.value = backupDataUseCase.importBackupFile(
                    backupFileUri = fileUri
                ) { progressPercent ->
                    com.ivy.base.coroutines.uiThread {
                        _importProgressPercent.value =
                            (progressPercent * 100).roundToInt()
                    }
                }
                _importStep.value = ImportStep.RESULT

            }
        }
    }

    fun skip() {
        nav.back()
        resetState()
    }

    fun finish() {
        nav.back()
        resetState()
    }

    private fun resetState() {
        _importStep.value = ImportStep.IMPORT_FROM
    }
}
