package com.ivy.planned.edit

sealed interface EditPlannedUiEvent {
    data object CloseScreen : EditPlannedUiEvent
}
