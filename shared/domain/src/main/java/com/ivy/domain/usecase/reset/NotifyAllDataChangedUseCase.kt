package com.ivy.domain.usecase.reset

import com.ivy.data.DataObserver
import com.ivy.data.DataWriteEvent
import javax.inject.Inject

class NotifyAllDataChangedUseCase @Inject constructor(
    private val dataObserver: DataObserver
) {
    suspend operator fun invoke() {
        dataObserver.post(DataWriteEvent.AllDataChange)
    }
}
