package com.ivy.planned.edit

internal sealed interface EditPlannedUiEvent {
    data object CloseScreen : EditPlannedUiEvent
}
