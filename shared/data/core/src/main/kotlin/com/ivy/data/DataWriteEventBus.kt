package com.ivy.data

import com.ivy.data.api.DataChangePublisher
import com.ivy.data.api.DataWriteEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DataWriteEventBus @Inject constructor() : DataChangePublisher {
    private val _writeEvents = MutableSharedFlow<DataWriteEvent>()
    override val writeEvents: Flow<DataWriteEvent> = _writeEvents

    override suspend fun post(event: DataWriteEvent) {
        _writeEvents.emit(event)
    }
}
