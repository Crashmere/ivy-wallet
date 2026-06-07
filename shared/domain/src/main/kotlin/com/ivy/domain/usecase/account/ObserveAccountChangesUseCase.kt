package com.ivy.domain.usecase.account

import com.ivy.data.api.DataChangePublisher
import com.ivy.data.api.DataWriteEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveAccountChangesUseCase @Inject constructor(
    private val dataChangePublisher: DataChangePublisher
) {
    operator fun invoke(): Flow<Unit> {
        return dataChangePublisher.writeEvents
            .filterIsInstance<DataWriteEvent.AccountChange>()
            .map { }
    }
}
