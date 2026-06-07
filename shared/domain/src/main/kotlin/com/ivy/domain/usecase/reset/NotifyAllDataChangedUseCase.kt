package com.ivy.domain.usecase.reset

import com.ivy.data.api.DataChangePublisher
import com.ivy.data.api.DataWriteEvent
import javax.inject.Inject

class NotifyAllDataChangedUseCase @Inject constructor(
    private val dataChangePublisher: DataChangePublisher
) {
    suspend operator fun invoke() {
        dataChangePublisher.post(DataWriteEvent.AllDataChange)
    }
}
